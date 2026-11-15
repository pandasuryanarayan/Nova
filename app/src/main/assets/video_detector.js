(function () {
var lastTriggerTimestamp = 0, touchTimer = null, touchStartX = 0, touchStartY = 0, lastTouchX = 0, lastTouchY = 0;
var TOUCH_HOLD_MS = 420, TOUCH_SLOP_PX = 22;
var DIRECT_VIDEO_REGEX = /\.(mp4|mov|webm|mkv|m4v|ogv)(\?|#|$)/i;
var MP4_PRIORITY_REGEX = /\.mp4(\?|#|$)/i;
var STREAMING_VIDEO_REGEX = /\.(m3u8|mpd|ism)(\?|#|$)/i;
var PROTECTED_DOMAIN_REGEX = /(youtube\.com|youtu\.be|instagram\.com|netflix\.com|disneyplus\.com|primevideo\.com|spotify\.com)/i;
function toAbsoluteUrl(rawUrl, baseDoc) {
if (!rawUrl || typeof rawUrl !== 'string') return '';
var trimmed = rawUrl.trim();
if (!trimmed || trimmed === 'about:blank' || trimmed.indexOf('javascript:') === 0) return '';
if (trimmed.indexOf('blob:') === 0 || trimmed.indexOf('data:') === 0 || trimmed.indexOf('mediasource:') === 0) return trimmed;
try {
var base = (baseDoc && (baseDoc.baseURI || (baseDoc.location && baseDoc.location.href))) ? (baseDoc.baseURI || baseDoc.location.href) : (document.baseURI || window.location.href);
return new URL(trimmed, base).href;
} catch (e) { return trimmed; }
}
function lookupPerformanceResourceSize(targetUrl) {
if (!targetUrl || !window.performance || typeof window.performance.getEntriesByType !== 'function') return -1;
try {
var entries = window.performance.getEntriesByType('resource') || [], cleanTarget = targetUrl.split('#')[0];
for (var i = entries.length - 1; i >= 0; i--) {
var r = entries[i];
if (!r || !r.name) continue;
if (r.name === targetUrl || r.name === cleanTarget || r.name.indexOf(cleanTarget) !== -1 || cleanTarget.indexOf(r.name) !== -1) {
var size = r.decodedBodySize || r.encodedBodySize || r.transferSize || 0;
if (size > 1024) return size;
}
}
} catch (e) {}
return -1;
}
var perfSize = lookupPerformanceResourceSize;
function inferQualityLabel(url, sourceEl, index) {
if (sourceEl) {
var explicit = sourceEl.getAttribute('label') || sourceEl.getAttribute('res') || sourceEl.getAttribute('size') || sourceEl.getAttribute('data-res') || sourceEl.getAttribute('data-quality') || sourceEl.getAttribute('title');
if (explicit) return /^\d+$/.test(explicit.trim()) ? (explicit.trim() + 'p') : explicit.trim();
}
if (url) {
var match = url.match(/(?:^|[^0-9])(2160p|1440p|1080p|720p|540p|480p|360p|240p|144p|4k|hd|sd)(?:[^0-9a-z]|$)/i);
if (match && match[1]) return match[1].toUpperCase();
var dimMatch = url.match(/(\d{3,4})x(\d{3,4})/i);
if (dimMatch && dimMatch[2]) return dimMatch[2] + 'p';
if (/\.mp4(\?|#|$)/i.test(url)) return index === 0 ? 'Direct MP4 (Default)' : ('MP4 Stream #' + (index + 1));
if (/\.ogg|ogv(\?|#|$)/i.test(url)) return 'OGG Video';
if (/\.webm(\?|#|$)/i.test(url)) return 'WebM Video';
}
return index === 0 ? 'Direct Video Stream' : ('Stream #' + (index + 1));
}
function classifyVideoUrl(url, mimeType) {
var lowerUrl = (url || '').toLowerCase(), lowerMime = (mimeType || '').toLowerCase();
var isBlobOrMediaSource = lowerUrl.indexOf('blob:') === 0 || lowerUrl.indexOf('mediasource:') === 0;
var isHlsOrDash = STREAMING_VIDEO_REGEX.test(lowerUrl) || lowerMime.indexOf('mpegurl') !== -1 || lowerMime.indexOf('dash+xml') !== -1;
var isProtectedSite = PROTECTED_DOMAIN_REGEX.test(lowerUrl) || PROTECTED_DOMAIN_REGEX.test(window.location.hostname || '');
var isStreaming = isBlobOrMediaSource || isHlsOrDash || isProtectedSite;
var isDirectMp4 = !isStreaming && (lowerUrl.indexOf('http://') === 0 || lowerUrl.indexOf('https://') === 0);
return { isStreaming: isStreaming, isDirectMp4: isDirectMp4, isPreferredMp4: !isStreaming && (MP4_PRIORITY_REGEX.test(lowerUrl) || lowerMime.indexOf('mp4') !== -1) };
}
function captureFramePoster(videoEl) {
if (!videoEl) return '';
var rawPoster = videoEl.getAttribute('poster') || videoEl.poster;
if (rawPoster) return toAbsoluteUrl(rawPoster, videoEl.ownerDocument);
try {
if (videoEl.videoWidth > 0 && videoEl.videoHeight > 0 && videoEl.readyState >= 2) {
var canvas = document.createElement('canvas'), targetW = Math.min(480, videoEl.videoWidth), targetH = Math.round((targetW / videoEl.videoWidth) * videoEl.videoHeight);
canvas.width = targetW; canvas.height = targetH;
canvas.getContext('2d').drawImage(videoEl, 0, 0, targetW, targetH);
var dataUrl = canvas.toDataURL('image/jpeg', 0.72);
if (dataUrl && dataUrl.length > 128) return dataUrl;
}
} catch (err) {}
return '';
}
function hitRect(r, x, y, pad) {
var p = pad || 32;
return r && r.width > 8 && r.height > 8 && x >= r.left - p && x <= r.right + p && y >= r.top - p && y <= r.bottom + p;
}
function isElementVisible(el, rect) {
if (!el) return false;
var r = rect || el.getBoundingClientRect();
if (!r || r.width < 12 || r.height < 12) return false;
try {
var style = window.getComputedStyle(el);
if (style) {
if (style.display === 'none' || style.visibility === 'hidden') return false;
var op = parseFloat(style.opacity);
if (!isNaN(op) && op <= 0.02) return false;
}
} catch (e) {}
return true;
}
var AD_URL_HINT_REGEX = /(doubleclick\.net|googlesyndication\.com|googleadservices\.com|exoclick\.com|popads\.|popcash\.|propellerads\.|teads\.tv|spotxchange\.|videoplaza\.|adcolony\.|outbrain\.|taboola\.|mgid\.|\/vast[^/?#]*\.xml|\/vmap[^/?#]*\.xml|\/pagead\/|\/adserver\/|preroll|midroll)/i;
var AD_ELEMENT_HINT_REGEX = /(^|[\s_-])(ad|ads|adv|advert|adverts|adsbox|ad-slot|ad-container|ad-wrapper|ad-player|adplayer|ad-video|advideo|preroll|midroll|vast|ima-ad)([\s_-]|$)/i;
function isLikelyAdVideo(videoEl, rawCandidates) {
if (!videoEl) return false;
try {
if (AD_ELEMENT_HINT_REGEX.test((videoEl.id || '') + ' ' + (videoEl.className || ''))) return true;
var p = videoEl.parentElement;
for (var d = 0; d < 4 && p; d++) {
if (p.tagName === 'INS' && p.classList && p.classList.contains('adsbygoogle')) return true;
if (p.hasAttribute && (p.hasAttribute('data-ad-slot') || p.hasAttribute('data-ad-client'))) return true;
if (AD_ELEMENT_HINT_REGEX.test((p.tagName || '') + ' ' + (p.id || '') + ' ' + (p.className || ''))) return true;
p = p.parentElement;
}
} catch (e) {}
if (rawCandidates && rawCandidates.length > 0) {
var allAdUrls = true;
for (var i = 0; i < rawCandidates.length; i++) {
if (!AD_URL_HINT_REGEX.test(rawCandidates[i].url || '')) { allAdUrls = false; break; }
}
if (allAdUrls) return true;
}
return false;
}
function scoreVid(videoEl, info, rect, clientX, clientY) {
var score = isElementVisible(videoEl, rect) ? 1000 : -1500;
if ((clientX > 0 || clientY > 0) && hitRect(rect, clientX, clientY, 32)) score += 550;
var hasDirectMp4 = false, hasRealMedia = false, isAd = isLikelyAdVideo(videoEl, info.qualities);
if (info.qualities) {
for (var i = 0; i < info.qualities.length; i++) {
var q = info.qualities[i];
if (q.isDirectMp4 && q.url && q.url.indexOf('http') === 0) hasDirectMp4 = true;
if (q.url && q.url.indexOf('/protected-stream') === -1) hasRealMedia = true;
}
}
score += hasDirectMp4 ? 450 : (hasRealMedia ? 250 : 0);
score += isAd ? -700 : 300;
try {
if (videoEl.readyState >= 2) score += 120;
if (videoEl.videoWidth > 0 && videoEl.videoHeight > 0) score += 120;
if (!videoEl.paused && !videoEl.ended) score += 180;
if (videoEl.currentTime > 0) score += 80;
if (videoEl.duration && isFinite(videoEl.duration)) {
if (videoEl.duration > 45) score += 160;
else if (videoEl.duration > 0 && videoEl.duration <= 20) score -= 80;
}
if (videoEl.error) score -= 400;
} catch (e) {}
if (rect && rect.width > 0 && rect.height > 0) score += Math.min(Math.round((rect.width * rect.height) / 2500), 120);
return score;
}
function collectVids(doc, isIframeDoc, outList, depth) {
if (!doc || (depth || 0) > 6) return;
var curDepth = depth || 0;
try {
var videos = doc.querySelectorAll ? doc.querySelectorAll('video') : [];
for (var i = 0; i < videos.length; i++) outList.push({ video: videos[i], doc: doc, isIframe: !!isIframeDoc });
} catch (e) {}
try {
var allEls = doc.querySelectorAll ? doc.querySelectorAll('*') : [];
for (var j = 0; j < allEls.length; j++) if (allEls[j].shadowRoot) collectVids(allEls[j].shadowRoot, isIframeDoc, outList, curDepth + 1);
} catch (e) {}
try {
var iframes = doc.querySelectorAll ? doc.querySelectorAll('iframe') : [];
for (var f = 0; f < iframes.length; f++) {
try {
var innerDoc = iframes[f].contentDocument || (iframes[f].contentWindow && iframes[f].contentWindow.document);
if (innerDoc) collectVids(innerDoc, true, outList, curDepth + 1);
} catch (ce) {}
}
} catch (e) {}
}
function extractVid(videoEl, isInsideIframe, allowEmptyPlaceholder) {
if (!videoEl) return null;
var doc = videoEl.ownerDocument || document, rawCandidates = [], seenUrls = {};
function addCandidate(rawUrl, mimeType, sourceEl) {
var abs = toAbsoluteUrl(rawUrl, doc);
if (!abs || seenUrls[abs]) return;
if (abs === window.location.href && (!rawUrl || rawUrl.trim() === '')) return;
seenUrls[abs] = true;
var classification = classifyVideoUrl(abs, mimeType);
rawCandidates.push({ url: abs, label: inferQualityLabel(abs, sourceEl, rawCandidates.length), mimeType: mimeType || 'video/mp4', isDirectMp4: classification.isDirectMp4, isStreaming: classification.isStreaming, isPreferredMp4: classification.isPreferredMp4, estimatedBytes: perfSize(abs) });
}
var sources = videoEl.querySelectorAll('source');
for (var i = 0; i < sources.length; i++) {
var s = sources[i], sUrl = s.getAttribute('src') || s.src || s.getAttribute('data-src') || s.getAttribute('data-url');
if (sUrl) addCandidate(sUrl, s.getAttribute('type') || s.type || '', s);
}
var attrSrc = videoEl.getAttribute('src');
if (attrSrc) addCandidate(attrSrc, videoEl.getAttribute('type') || 'video/mp4', null);
if (videoEl.currentSrc) addCandidate(videoEl.currentSrc, videoEl.getAttribute('type') || 'video/mp4', null);
if (videoEl.src && videoEl.src !== window.location.href) addCandidate(videoEl.src, videoEl.getAttribute('type') || 'video/mp4', null);
var dataAttrs = ['data-src', 'data-video', 'data-mp4', 'data-hd-src', 'data-sd-src', 'data-url', 'data-file'];
for (var d = 0; d < dataAttrs.length; d++) {
var val = videoEl.getAttribute(dataAttrs[d]);
if (val) addCandidate(val, 'video/mp4', null);
}
var container = videoEl.closest ? (videoEl.closest('div, section, article, figure, main') || doc.body) : doc.body;
if (container) {
var nearbyLinks = container.querySelectorAll('a[href]');
for (var j = 0; j < Math.min(nearbyLinks.length, 30); j++) {
var link = nearbyLinks[j], href = link.getAttribute('href') || '';
if (DIRECT_VIDEO_REGEX.test(href) && !AD_URL_HINT_REGEX.test(href)) {
var absLink = toAbsoluteUrl(href, doc);
if (absLink && !seenUrls[absLink]) {
seenUrls[absLink] = true;
var linkText = (link.textContent || '').trim();
rawCandidates.push({ url: absLink, label: (linkText && linkText.length <= 28) ? linkText : inferQualityLabel(absLink, link, rawCandidates.length), mimeType: 'video/mp4', isDirectMp4: true, isStreaming: false, isPreferredMp4: MP4_PRIORITY_REGEX.test(absLink), fromLink: true, estimatedBytes: perfSize(absLink) });
}
}
}
}
rawCandidates.sort(function (a, b) {
if (!!a.fromLink !== !!b.fromLink && a.isDirectMp4 && b.isDirectMp4) return a.fromLink ? 1 : -1;
if (a.isPreferredMp4 !== b.isPreferredMp4) return a.isPreferredMp4 ? -1 : 1;
if (a.isDirectMp4 !== b.isDirectMp4) return a.isDirectMp4 ? -1 : 1;
return 0;
});
var primaryUrl = rawCandidates.length > 0 ? rawCandidates[0].url : '';
var isProtectedPage = PROTECTED_DOMAIN_REGEX.test(window.location.hostname || '');
if (!primaryUrl) {
var hasActiveStream = isProtectedPage || videoEl.readyState > 0 || videoEl.videoWidth > 0;
if (allowEmptyPlaceholder === false || !hasActiveStream) return null;
primaryUrl = 'blob:' + window.location.origin + '/protected-stream';
rawCandidates.push({ url: primaryUrl, label: 'Protected Stream', mimeType: 'application/x-mpegURL', isDirectMp4: false, isStreaming: true, estimatedBytes: -1 });
} else if (isProtectedPage) {
for (var k = 0; k < rawCandidates.length; k++) { rawCandidates[k].isStreaming = true; rawCandidates[k].isDirectMp4 = false; }
}
var dims = (videoEl.videoWidth && videoEl.videoHeight) ? (videoEl.videoWidth + 'x' + videoEl.videoHeight) : '';
var dur = (videoEl.duration && isFinite(videoEl.duration) && videoEl.duration > 0) ? Math.round(videoEl.duration) : 0;
return { pageTitle: document.title || window.location.hostname || 'video', pageUrl: window.location.href, primaryUrl: primaryUrl, posterUrl: captureFramePoster(videoEl), qualities: rawCandidates, isIframe: !!isInsideIframe, dimensions: dims, durationSeconds: dur };
}
function extractFromAnchorUrl(absUrl, anchorEl) {
var classification = classifyVideoUrl(absUrl, 'video/mp4');
var label = (anchorEl && anchorEl.textContent ? anchorEl.textContent.trim().slice(0, 30) : '') || inferQualityLabel(absUrl, anchorEl, 0);
return { pageTitle: document.title || window.location.hostname || 'video', pageUrl: window.location.href, primaryUrl: absUrl, posterUrl: '', qualities: [{ url: absUrl, label: label || 'Direct MP4', mimeType: 'video/mp4', isDirectMp4: classification.isDirectMp4, isStreaming: classification.isStreaming, estimatedBytes: perfSize(absUrl) }], isIframe: false, dimensions: '', durationSeconds: 0 };
}
function findVidAt(doc, clientX, clientY, isIframeDoc) {
if (!doc) return null;
var bestMatch = null;
var bestScore = -999999;
var seenVideos = [];

function considerVideo(vEl, iframeFlag) {
if (!vEl || seenVideos.indexOf(vEl) !== -1) return;
seenVideos.push(vEl);
var rect = vEl.getBoundingClientRect(), info = extractBest(vEl, iframeFlag);
if (!info) return;
var s = scoreVid(vEl, info, rect, clientX, clientY);
if (s > bestScore) { bestScore = s; bestMatch = info; }
}
if (typeof doc.elementsFromPoint === 'function' && (clientX > 0 || clientY > 0)) {
var els = doc.elementsFromPoint(clientX, clientY);
for (var i = 0; i < els.length; i++) {
var el = els[i];
if (!el || !el.tagName) continue;
if (el.tagName.toUpperCase() === 'VIDEO') considerVideo(el, isIframeDoc);
var closestVideo = el.closest ? el.closest('video') : null;
if (closestVideo) considerVideo(closestVideo, isIframeDoc);
if (el.querySelectorAll) {
var childVideos = el.querySelectorAll('video');
for (var cv = 0; cv < childVideos.length; cv++) if (hitRect(childVideos[cv].getBoundingClientRect(), clientX, clientY, 32)) considerVideo(childVideos[cv], isIframeDoc);
}
if (el.shadowRoot && el.shadowRoot.querySelectorAll) {
var shadowVids = el.shadowRoot.querySelectorAll('video');
for (var sv = 0; sv < shadowVids.length; sv++) if (hitRect(shadowVids[sv].getBoundingClientRect(), clientX, clientY, 32)) considerVideo(shadowVids[sv], isIframeDoc);
}
}
}
var collected = [];
collectVids(doc, isIframeDoc, collected, 0);
for (var v = 0; v < collected.length; v++) {
var item = collected[v];
if (hitRect(item.video.getBoundingClientRect(), clientX, clientY, 32)) considerVideo(item.video, item.isIframe);
}
if (bestMatch) return bestMatch;
if (typeof doc.elementsFromPoint === 'function' && (clientX > 0 || clientY > 0)) {
var els2 = doc.elementsFromPoint(clientX, clientY);
for (var a = 0; a < els2.length; a++) {
var closestAnchor = els2[a].closest ? els2[a].closest('a[href]') : null;
if (closestAnchor) {
var href = toAbsoluteUrl(closestAnchor.getAttribute('href'), doc);
if (DIRECT_VIDEO_REGEX.test(href) || STREAMING_VIDEO_REGEX.test(href) || /content-type=video|format=mp4/i.test(href)) return extractFromAnchorUrl(href, closestAnchor);
}
}
}
var iframes = doc.querySelectorAll ? doc.querySelectorAll('iframe') : [];
for (var f = 0; f < iframes.length; f++) {
var iframe = iframes[f], iRect = iframe.getBoundingClientRect();
if (hitRect(iRect, clientX, clientY, 16)) {
try {
var innerDoc = iframe.contentDocument || (iframe.contentWindow && iframe.contentWindow.document);
if (innerDoc) {
var localX = clientX - iRect.left, localY = clientY - iRect.top;
var innerHit = findVidAt(innerDoc, localX, localY, true);
if (innerHit) return innerHit;
var anyInnerVideos = innerDoc.querySelectorAll('video'), bestInner = null, bestInnerScore = -999999;
for (var iv = 0; iv < anyInnerVideos.length; iv++) {
var iVid = anyInnerVideos[iv], iVidInfo = extractBest(iVid, true);
if (iVidInfo) {
var iVidScore = scoreVid(iVid, iVidInfo, iVid.getBoundingClientRect(), localX, localY);
if (iVidScore > bestInnerScore) { bestInnerScore = iVidScore; bestInner = iVidInfo; }
}
}
if (bestInner) return bestInner;
}
} catch (crossOriginErr) {}
var iframeSrc = toAbsoluteUrl(iframe.getAttribute('src') || iframe.src, doc);
if (iframeSrc) {
if (DIRECT_VIDEO_REGEX.test(iframeSrc) || STREAMING_VIDEO_REGEX.test(iframeSrc)) {
var info = extractFromAnchorUrl(iframeSrc, iframe);
info.isIframe = true;
return info;
}
if (PROTECTED_DOMAIN_REGEX.test(iframeSrc) || /player\.vimeo\.com|dailymotion\.com|embed/i.test(iframeSrc)) {
return { pageTitle: document.title || 'Embedded Video', pageUrl: window.location.href, primaryUrl: 'blob:' + iframeSrc, posterUrl: '', qualities: [{ url: 'blob:' + iframeSrc, label: 'Protected Iframe Stream', mimeType: 'application/x-mpegURL', isDirectMp4: false, isStreaming: true, estimatedBytes: -1 }], isIframe: true, dimensions: '', durationSeconds: 0 };
}
}
}
}
return null;
}
function dispatchToAndroid(payload) {
if (!payload || !payload.primaryUrl) return false;
var now = Date.now();
if (now - lastTriggerTimestamp < 750) return true;
lastTriggerTimestamp = now;
try {
if (window.NetraVideoBridge && typeof window.NetraVideoBridge.onVideoLongPressed === 'function') {
window.NetraVideoBridge.onVideoLongPressed(JSON.stringify(payload));
return true;
}
} catch (e) { console.error('NetraVideoBridge error:', e); }
return false;
}
function fetchVideoWithCredentialsFallback(videoUrl, headersObj, signal) {
function tryCreds(credMode) {
var opts = {
method: 'GET',
credentials: credMode,
headers: headersObj || {}
};
if (signal) opts.signal = signal;
return fetch(videoUrl, opts).then(function (resp) {
if (!resp.ok && resp.status !== 206) {
throw new Error('HTTP ' + resp.status);
}
return resp;
});
}
return tryCreds('same-origin').catch(function () { return tryCreds('include'); }).catch(function () { return tryCreds('omit'); });
}
var fetchCreds = fetchVideoWithCredentialsFallback;
window.__netraProbeVideoSizeInWebView = function (videoUrl) {
if (!videoUrl || videoUrl.indexOf('blob:') === 0) return;
var perfSize = lookupPerformanceResourceSize(videoUrl);
if (perfSize > 1024 && window.NetraVideoBridge && window.NetraVideoBridge.onVideoSizeProbed) {
window.NetraVideoBridge.onVideoSizeProbed(JSON.stringify({
url: videoUrl,
contentLength: perfSize,
contentType: 'video/mp4'
}));
}
var controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
var sig = controller ? controller.signal : null;
fetchVideoWithCredentialsFallback(videoUrl, { 'Range': 'bytes=0-1' }, sig).then(function (resp) {
var cType = resp.headers.get('Content-Type') || 'video/mp4';
var cRange = resp.headers.get('Content-Range') || '';
var totalSize = -1;
if (cRange && cRange.indexOf('/') !== -1) {
var afterSlash = parseInt(cRange.split('/').pop().trim(), 10);
if (!isNaN(afterSlash) && afterSlash > 0) {
totalSize = afterSlash;
}
}
if (totalSize <= 0 && resp.status === 200) {
var cLen = parseInt(resp.headers.get('Content-Length') || '-1', 10);
if (!isNaN(cLen) && cLen > 1024) {
totalSize = cLen;
}
}
if (controller) {
try { controller.abort(); } catch (e) {}
}
if (totalSize <= 0) {
totalSize = lookupPerformanceResourceSize(videoUrl);
}
if (totalSize > 0 && window.NetraVideoBridge && window.NetraVideoBridge.onVideoSizeProbed) {
window.NetraVideoBridge.onVideoSizeProbed(JSON.stringify({
url: videoUrl,
contentLength: totalSize,
contentType: cType.split(';')[0].trim()
}));
}
}).catch(function () {
var fallbackPerf = lookupPerformanceResourceSize(videoUrl);
if (fallbackPerf > 1024 && window.NetraVideoBridge && window.NetraVideoBridge.onVideoSizeProbed) {
window.NetraVideoBridge.onVideoSizeProbed(JSON.stringify({
url: videoUrl,
contentLength: fallbackPerf,
contentType: 'video/mp4'
}));
}
});
};
function uint8ToBase64(uint8) {
var CHUNK_SZ = 0x8000, c = [];
for (var i = 0; i < uint8.length; i += CHUNK_SZ) c.push(String.fromCharCode.apply(null, uint8.subarray(i, i + CHUNK_SZ)));
return btoa(c.join(''));
}
window.__netraDownloadVideoViaWebView = function (downloadId, videoUrl) {
if (!window.NetraVideoBridge) return;
fetchCreds(videoUrl, { 'Accept': 'video/mp4,video/*;q=0.9,*/*;q=0.8' }, null).then(function (response) {
var totalBytes = parseInt(response.headers.get('Content-Length') || '-1', 10), cRange = response.headers.get('Content-Range') || '';
if (totalBytes <= 0 && cRange.indexOf('/') !== -1) {
var parsedTotal = parseInt(cRange.split('/').pop().trim(), 10);
if (!isNaN(parsedTotal) && parsedTotal > 0) totalBytes = parsedTotal;
}
if (totalBytes <= 0) totalBytes = perfSize(videoUrl);
if (!response.body || typeof response.body.getReader !== 'function') {
return response.arrayBuffer().then(function (buf) {
var bytes = new Uint8Array(buf), step = 98304;
for (var offset = 0; offset < bytes.length; offset += step) {
window.NetraVideoBridge.onWebViewDownloadChunk(downloadId, uint8ToBase64(bytes.subarray(offset, Math.min(bytes.length, offset + step))), Math.min(bytes.length, offset + step), bytes.length);
}
window.NetraVideoBridge.onWebViewDownloadFinished(downloadId, bytes.length);
});
}
var reader = response.body.getReader(), loaded = 0;
function pump() {
return reader.read().then(function (res) {
if (res.done) { window.NetraVideoBridge.onWebViewDownloadFinished(downloadId, loaded); return; }
var val = res.value;
if (val && val.length > 0) {
loaded += val.length;
var step = 98304;
for (var offset = 0; offset < val.length; offset += step) {
window.NetraVideoBridge.onWebViewDownloadChunk(downloadId, uint8ToBase64(val.subarray(offset, Math.min(val.length, offset + step))), loaded, totalBytes > 0 ? totalBytes : -1);
}
}
return pump();
});
}
return pump();
}).catch(function (err) {
if (window.NetraVideoBridge && window.NetraVideoBridge.onWebViewDownloadError) {
window.NetraVideoBridge.onWebViewDownloadError(downloadId, String(err && err.message ? err.message : err));
}
});
};
function extractBest(videoEl, isIframeDoc) {
return extractVid(videoEl, isIframeDoc, false) || extractVid(videoEl, isIframeDoc, true);
}
function bindVid(videoEl, isIframeDoc) {
if (!videoEl || videoEl.__netraDirectBound) return;
videoEl.__netraDirectBound = true;
videoEl.style.webkitTouchCallout = 'none';
videoEl.style.webkitUserSelect = 'none';
var localTimer = null, startX = 0, startY = 0;
function triggerAt(x, y) {
var doc = videoEl.ownerDocument || document;
var info = findVidAt(doc, x, y, isIframeDoc) || extractBest(videoEl, isIframeDoc);
if (info) dispatchToAndroid(info);
}
videoEl.addEventListener('contextmenu', function (e) {
e.preventDefault();
e.stopPropagation();
triggerAt(e.clientX || startX || lastTouchX, e.clientY || startY || lastTouchY);
return false;
}, true);
videoEl.addEventListener('touchstart', function (e) {
if (!e.touches || e.touches.length !== 1) return;
startX = e.touches[0].clientX;
startY = e.touches[0].clientY;
lastTouchX = startX;
lastTouchY = startY;
if (localTimer) clearTimeout(localTimer);
localTimer = setTimeout(function () { localTimer = null; triggerAt(startX, startY); }, TOUCH_HOLD_MS);
}, { capture: true, passive: true });
videoEl.addEventListener('touchmove', function (e) {
if (!e.touches || !e.touches.length) return;
if (Math.abs(e.touches[0].clientX - startX) > TOUCH_SLOP_PX || Math.abs(e.touches[0].clientY - startY) > TOUCH_SLOP_PX) {
if (localTimer) { clearTimeout(localTimer); localTimer = null; }
}
}, { capture: true, passive: true });
var clearLocal = function () { if (localTimer) { clearTimeout(localTimer); localTimer = null; } };
videoEl.addEventListener('touchend', clearLocal, { capture: true, passive: true });
videoEl.addEventListener('touchcancel', clearLocal, { capture: true, passive: true });
}
function bindDoc(doc, isIframeDoc) {
if (!doc || doc.__netraListenersAttached) return;
doc.__netraListenersAttached = true;
var eventRoot = (!isIframeDoc && doc === document && typeof window !== 'undefined') ? window : doc;
eventRoot.addEventListener('contextmenu', function (e) {
var x = e.clientX || lastTouchX, y = e.clientY || lastTouchY;
var hit = findVidAt(doc, x, y, isIframeDoc);
if (!hit && e.target) {
var tv = (e.target.tagName && e.target.tagName.toUpperCase() === 'VIDEO') ? e.target : (e.target.closest ? e.target.closest('video') : null);
if (tv) hit = extractBest(tv, isIframeDoc);
else if (e.target.querySelectorAll) {
var cands = e.target.querySelectorAll('video'), bestCS = -999999;
for (var ci = 0; ci < cands.length; ci++) {
var cInfo = extractBest(cands[ci], isIframeDoc);
if (cInfo) {
var cs = scoreVid(cands[ci], cInfo, cands[ci].getBoundingClientRect(), x, y);
if (cs > bestCS) { bestCS = cs; hit = cInfo; }
}
}
}
}
if (hit) { e.preventDefault(); e.stopPropagation(); dispatchToAndroid(hit); return false; }
}, true);
eventRoot.addEventListener('touchstart', function (e) {
if (!e.touches || e.touches.length !== 1) { if (touchTimer) { clearTimeout(touchTimer); touchTimer = null; } return; }
var t = e.touches[0];
touchStartX = t.clientX; touchStartY = t.clientY; lastTouchX = t.clientX; lastTouchY = t.clientY;
if (touchTimer) clearTimeout(touchTimer);
touchTimer = setTimeout(function () {
touchTimer = null;
var hit = findVidAt(doc, touchStartX, touchStartY, isIframeDoc);
if (hit) dispatchToAndroid(hit);
}, TOUCH_HOLD_MS);
}, { capture: true, passive: true });
eventRoot.addEventListener('touchmove', function (e) {
if (!e.touches || !e.touches.length) return;
var t = e.touches[0];
lastTouchX = t.clientX; lastTouchY = t.clientY;
if (Math.abs(t.clientX - touchStartX) > TOUCH_SLOP_PX || Math.abs(t.clientY - touchStartY) > TOUCH_SLOP_PX) {
if (touchTimer) { clearTimeout(touchTimer); touchTimer = null; }
}
}, { capture: true, passive: true });
var clearTT = function () { if (touchTimer) { clearTimeout(touchTimer); touchTimer = null; } };
eventRoot.addEventListener('touchend', clearTT, { capture: true, passive: true });
eventRoot.addEventListener('touchcancel', clearTT, { capture: true, passive: true });
}
function scanDoc(doc, isIframeDoc) {
if (!doc) return;
bindDoc(doc, isIframeDoc);
var collected = [];
collectVids(doc, isIframeDoc, collected, 0);
for (var i = 0; i < collected.length; i++) bindVid(collected[i].video, collected[i].isIframe);
}
window.__netraInspectVideoAtLastTouch = function (fallbackX, fallbackY) {
var tx = (lastTouchX > 0 || lastTouchY > 0) ? lastTouchX : (fallbackX || 0);
var ty = (lastTouchX > 0 || lastTouchY > 0) ? lastTouchY : (fallbackY || 0);
var hit = findVidAt(document, tx, ty, false);
if (!hit) {
var collected = [], bestScore = -999999;
collectVids(document, false, collected, 0);
for (var i = 0; i < collected.length; i++) {
var item = collected[i], r = item.video.getBoundingClientRect();
if (!isElementVisible(item.video, r) || r.bottom <= 0 || r.top >= window.innerHeight) continue;
var info = extractBest(item.video, item.isIframe);
if (info) {
var s = scoreVid(item.video, info, r, tx, ty);
if (s > bestScore) { bestScore = s; hit = info; }
}
}
}
if (hit) { dispatchToAndroid(hit); return JSON.stringify(hit); }
return '';
};
window.__netraScanAllPageVideos = function () {
var collected = [], scored = [];
collectVids(document, false, collected, 0);
for (var i = 0; i < collected.length; i++) {
var item = collected[i], r = item.video.getBoundingClientRect(), info = extractVid(item.video, item.isIframe, false);
if (!info) continue;
if (!isElementVisible(item.video, r) && (!info.qualities || info.qualities.length === 0)) continue;
scored.push({ score: scoreVid(item.video, info, r, -1, -1), info: info });
}
if (scored.length === 0) {
for (var j = 0; j < collected.length; j++) {
var fi = extractVid(collected[j].video, collected[j].isIframe, true);
if (fi) scored.push({ score: scoreVid(collected[j].video, fi, collected[j].video.getBoundingClientRect(), -1, -1), info: fi });
}
}
scored.sort(function (a, b) { return b.score - a.score; });
var results = [];
for (var k = 0; k < scored.length; k++) results.push(scored[k].info);
return JSON.stringify(results);
};
scanDoc(document, false);
if (!window.__netraMutationObserverStarted && typeof MutationObserver !== 'undefined' && document.documentElement) {
window.__netraMutationObserverStarted = true;
new MutationObserver(function () { scanDoc(document, false); }).observe(document.documentElement, { childList: true, subtree: true });
}
})();
