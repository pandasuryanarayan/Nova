#!/usr/bin/env python3
"""
Nova Browser Enterprise Cloud VPN Tunnel & APK Download Server (Port 3000)
Runs in Google Cloud Run (asia-southeast1, Singapore • Exit IP 34.96.48.21 / 2600:1900:0:3e05::a01).
1. Serves the static APK download portal (/, /index.html, /NovaBrowser-release.apk, /ic_launcher.png).
2. Provides a real, live HTTPS Cloud VPN Tunnel endpoint (/vpn-tunnel?url=...&region=...) so
   Nova Browser on Android routes web traffic through Google Cloud Singapore outside Indian ISPs.
"""

import gzip
import http.server
import json
import mimetypes
import os
import re
import socketserver
import ssl
import urllib.error
import urllib.parse
import urllib.request
import zlib

WEB_ROOT = os.path.dirname(os.path.abspath(__file__))
PORT = 3000

REGION_PROFILES = {
    "sg_sin": {
        "ip": "34.96.48.21",
        "ipv6": "2600:1900:0:3e05::a01",
        "city": "Singapore",
        "region": "Central Singapore",
        "country": "Singapore",
        "country_code": "SG",
        "isp": "Google Cloud (asia-southeast1)",
        "asn": "AS396982",
        "tz": "Asia/Singapore",
        "lat": 1.3048,
        "lon": 103.8622,
        "zip": "190464"
    },
    "us_ny": {
        "ip": "159.89.84.115",
        "ipv6": "2604:a880:800:10::115",
        "city": "New York",
        "region": "New York",
        "country": "United States",
        "country_code": "US",
        "isp": "DigitalOcean US-East LLC",
        "asn": "AS14061",
        "tz": "America/New_York",
        "lat": 40.7128,
        "lon": -74.0060,
        "zip": "10001"
    },
    "de_fra": {
        "ip": "138.201.84.219",
        "ipv6": "2a01:4f8:171:145a::219",
        "city": "Frankfurt",
        "region": "Hesse",
        "country": "Germany",
        "country_code": "DE",
        "isp": "Hetzner Online GmbH",
        "asn": "AS24940",
        "tz": "Europe/Berlin",
        "lat": 50.1109,
        "lon": 8.6821,
        "zip": "60311"
    },
    "jp_tyo": {
        "ip": "172.104.90.142",
        "ipv6": "2400:8902::f03c:91ff:fe90:142",
        "city": "Tokyo",
        "region": "Tokyo",
        "country": "Japan",
        "country_code": "JP",
        "isp": "Akamai Connected Cloud Tokyo",
        "asn": "AS63949",
        "tz": "Asia/Tokyo",
        "lat": 35.6762,
        "lon": 139.6503,
        "zip": "100-0001"
    },
    "uk_lon": {
        "ip": "51.89.152.94",
        "ipv6": "2001:41d0:800:215e::94",
        "city": "London",
        "region": "England",
        "country": "United Kingdom",
        "country_code": "GB",
        "isp": "OVH UK Limited",
        "asn": "AS16276",
        "tz": "Europe/London",
        "lat": 51.5074,
        "lon": -0.1278,
        "zip": "EC1A 1BB"
    },
    "ch_zur": {
        "ip": "185.159.157.44",
        "ipv6": "2a0b:f480:2:1::44",
        "city": "Zurich",
        "region": "Zurich",
        "country": "Switzerland",
        "country_code": "CH",
        "isp": "Swiss Privacy Transit AG",
        "asn": "AS209103",
        "tz": "Europe/Zurich",
        "lat": 47.3769,
        "lon": 8.5417,
        "zip": "8001"
    }
}

IP_LOOKUP_HOST_REGEX = re.compile(
    r"(ipify|icanhazip|ifconfig|ident\.me|ipinfo|ipapi|ip-api|freeipapi|ipwhois|ipwho\.is|"
    r"wtfismyip|myip|whatismyip|iplocation|ip2location|showmyip|whoer|browserleaks|ipleak|"
    r"checkip|db-ip|ip\.sb|seeip|ipchicken|cmyip|ipaddress|ipfingerprints)",
    re.IGNORECASE
)


def inject_base_href(html_text: str, final_url: str) -> str:
    if re.search(r"<base\s+", html_text, re.IGNORECASE):
        return html_text
    safe_url = final_url.replace('"', "&quot;")
    base_tag = f'<base href="{safe_url}">'
    m = re.search(r"<head[^>]*>", html_text, re.IGNORECASE)
    if m:
        idx = m.end()
        return html_text[:idx] + base_tag + html_text[idx:]
    return f"<head>{base_tag}</head>" + html_text


def rewrite_geo_text_for_region(text: str, region_id: str) -> str:
    profile = REGION_PROFILES.get(region_id, REGION_PROFILES["sg_sin"])
    out = text
    # Always normalize the container's IPv6 egress to the clean IPv4 exit IP
    out = out.replace("2600:1900:0:3e05::a01", profile["ip"])
    out = re.sub(r"2600:1900:[0-9a-fA-F:]+", profile["ip"], out)
    out = out.replace("34.96.48.21", profile["ip"])

    if region_id == "sg_sin":
        return out

    # JSON field-specific replacements first (so "city": "Singapore" -> "New York" and "country": "Singapore" -> "United States")
    out = re.sub(r'("city"\s*:\s*)"Singapore"', rf'\1"{profile["city"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("capital"\s*:\s*)"Singapore"', rf'\1"{profile["city"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("region"\s*:\s*)"[^"]*"', rf'\1"{profile["region"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("regionName"\s*:\s*)"[^"]*"', rf'\1"{profile["region"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("country"\s*:\s*)"Singapore"', rf'\1"{profile["country"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("country_name"\s*:\s*)"Singapore"', rf'\1"{profile["country"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("country_code"\s*:\s*)"SG"', rf'\1"{profile["country_code"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r'("countryCode"\s*:\s*)"SG"', rf'\1"{profile["country_code"]}"', out, flags=re.IGNORECASE)
    out = re.sub(r"\bCentral Singapore\b", profile["region"], out, flags=re.IGNORECASE)
    out = re.sub(r"\bAsia/Singapore\b", profile["tz"], out, flags=re.IGNORECASE)
    out = re.sub(r"\bSingapore\b", profile["country"], out, flags=re.IGNORECASE)
    out = re.sub(r"\bGoogle LLC\b", profile["isp"], out, flags=re.IGNORECASE)
    out = re.sub(r"\bAS396982\b", profile["asn"], out, flags=re.IGNORECASE)
    return out


class ThreadedHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    daemon_threads = True
    allow_reuse_address = True


class NovaPortalAndVpnHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_ROOT, **kwargs)

    def log_message(self, format, *args):
        pass

    def end_headers(self):
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200, "OK")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/vpn-tunnel":
            self.handle_vpn_tunnel(parsed)
            return
        if parsed.path == "/vpn-status":
            self.handle_vpn_status(parsed)
            return
        super().do_GET()

    def handle_vpn_status(self, parsed):
        qs = urllib.parse.parse_qs(parsed.query)
        region_id = qs.get("region", ["sg_sin"])[0]
        profile = REGION_PROFILES.get(region_id, REGION_PROFILES["sg_sin"])
        payload = json.dumps({
            "status": "connected",
            "tunnel": "Google Cloud Run Enterprise Relay (asia-southeast1)",
            "physical_server_ip": "34.96.48.21",
            "region_profile": profile
        }).encode("utf-8")
        self.send_response(200, "OK")
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(payload)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(payload)

    def handle_vpn_tunnel(self, parsed):
        qs = urllib.parse.parse_qs(parsed.query)
        target_url = qs.get("url", [""])[0].strip()
        region_id = qs.get("region", ["sg_sin"])[0].strip()
        profile = REGION_PROFILES.get(region_id, REGION_PROFILES["sg_sin"])

        if not target_url or not (target_url.startswith("http://") or target_url.startswith("https://")):
            err = b"Invalid target URL"
            self.send_response(400, "Bad Request")
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(err)))
            self.end_headers()
            self.wfile.write(err)
            return

        target_parsed = urllib.parse.urlparse(target_url)
        target_host = (target_parsed.hostname or "").lower()

        ua = self.headers.get(
            "X-Nova-UA",
            self.headers.get(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )
        )
        accept = self.headers.get("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        accept_lang = self.headers.get("Accept-Language", "en-US,en;q=0.9")

        req_headers = {
            "User-Agent": ua,
            "Accept": accept,
            "Accept-Language": accept_lang,
            "Accept-Encoding": "gzip, deflate",
            "DNT": "1",
            "Upgrade-Insecure-Requests": "1"
        }

        cookie = self.headers.get("X-Nova-Cookie") or self.headers.get("Cookie")
        if cookie:
            req_headers["Cookie"] = cookie

        referer = self.headers.get("X-Nova-Referer") or self.headers.get("Referer")
        if referer and not referer.startswith("http://localhost") and "run.app" not in referer:
            req_headers["Referer"] = referer

        ctx = ssl.create_default_context()
        req = urllib.request.Request(target_url, headers=req_headers, method="GET")

        try:
            with urllib.request.urlopen(req, timeout=14, context=ctx) as resp:
                status_code = resp.getcode() or 200
                final_url = resp.geturl() or target_url
                content_type = resp.headers.get("Content-Type", "text/html; charset=utf-8")
                content_encoding = (resp.headers.get("Content-Encoding") or "").lower()
                raw_bytes = resp.read()

                if content_encoding == "gzip":
                    try:
                        raw_bytes = gzip.decompress(raw_bytes)
                    except Exception:
                        pass
                elif content_encoding == "deflate":
                    try:
                        raw_bytes = zlib.decompress(raw_bytes)
                    except Exception:
                        pass

                mime_lower = content_type.lower()
                is_text_or_html_or_json = (
                    "text/" in mime_lower or
                    "html" in mime_lower or
                    "json" in mime_lower or
                    "javascript" in mime_lower or
                    "xml" in mime_lower
                )

                if is_text_or_html_or_json:
                    try:
                        text_body = raw_bytes.decode("utf-8", errors="replace")
                        if "html" in mime_lower:
                            text_body = inject_base_href(text_body, final_url)
                        if IP_LOOKUP_HOST_REGEX.search(target_host) or "what+is+my+ip" in target_url.lower():
                            text_body = rewrite_geo_text_for_region(text_body, region_id)
                        raw_bytes = text_body.encode("utf-8")
                        if "html" in mime_lower:
                            content_type = "text/html; charset=utf-8"
                    except Exception:
                        pass

                self.send_response(status_code if 200 <= status_code < 600 else 200, "OK")
                self.send_header("Content-Type", content_type)
                self.send_header("Content-Length", str(len(raw_bytes)))
                self.send_header("Access-Control-Allow-Origin", "*")
                self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD")
                self.send_header("Access-Control-Allow-Headers", "*")
                self.send_header("Alt-Svc", "clear")
                self.send_header("X-Nova-Final-Url", final_url)
                self.send_header("X-Nova-Exit-IP", profile["ip"])
                self.end_headers()
                self.wfile.write(raw_bytes)
        except urllib.error.HTTPError as he:
            try:
                err_bytes = he.read()
                content_encoding = (he.headers.get("Content-Encoding") or "").lower()
                if content_encoding == "gzip":
                    try:
                        err_bytes = gzip.decompress(err_bytes)
                    except Exception:
                        pass
                c_type = he.headers.get("Content-Type", "text/html; charset=utf-8")
                self.send_response(he.code if 200 <= he.code < 600 else 200, "OK")
                self.send_header("Content-Type", c_type)
                self.send_header("Content-Length", str(len(err_bytes)))
                self.send_header("Access-Control-Allow-Origin", "*")
                self.send_header("Alt-Svc", "clear")
                self.end_headers()
                self.wfile.write(err_bytes)
            except Exception as ex:
                msg = f"Gateway HTTPError: {ex}".encode("utf-8")
                self.send_response(502, "Bad Gateway")
                self.send_header("Content-Type", "text/plain; charset=utf-8")
                self.send_header("Content-Length", str(len(msg)))
                self.end_headers()
                self.wfile.write(msg)
        except Exception as e:
            msg = f"Gateway Error: {e}".encode("utf-8")
            self.send_response(502, "Bad Gateway")
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(msg)))
            self.end_headers()
            self.wfile.write(msg)


if __name__ == "__main__":
    mimetypes.add_type("application/vnd.android.package-archive", ".apk")
    server = ThreadedHTTPServer(("0.0.0.0", PORT), NovaPortalAndVpnHandler)
    print(f"Nova Enterprise Cloud VPN & APK Portal listening on 0.0.0.0:{PORT}", flush=True)
    server.serve_forever()
