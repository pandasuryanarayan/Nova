(function () {
var lastTriggerTimestamp = 0;
var touchTimer = null;
var touchStartX = 0;
var touchStartY = 0;
var lastTouchX = 0;
var lastTouchY = 0;
var TOUCH_HOLD_MS = 420;
var TOUCH_SLOP_PX = 22;
var DIRECT_VIDEO_REGEX = /\.(mp4|mov|webm|mkv|m4v|ogv)(\?|#|$)/i;
var MP4_PRIORITY_REGEX = /\.mp4(\?|#|$)/i;
var STREAMING_VIDEO_REGEX = /\.(m3u8|mpd|ism)(\?|#|$)/i;
var PROTECTED_DOMAIN_REGEX = /(youtube\.com|youtu\.be|instagram\.com|netflix\.com|disneyplus\.com|primevideo\.com|spotify\.com)/i;
function toAbsoluteUrl(rawUrl, baseDoc) {
if (!rawUrl || typeof rawUrl !== 'string') return '';
var trimmed = rawUrl.trim();
if (!trimmed || trimmed === 'about:blank' || trimmed.indexOf('javascript:') === 0) {
return '';
}
if (trimmed.indexOf('blob:') === 0 || trimmed.indexOf('data:') === 0 || trimmed.indexOf('mediasource:') === 0) {
return trimmed;
}
try {
var base = (baseDoc && (baseDoc.baseURI || (baseDoc.location && baseDoc.location.href)))
? (baseDoc.baseURI || baseDoc.location.href)
: (document.baseURI || window.location.href);
return new URL(trimmed, base).href;
} catch (e) {
return trimmed;
}
}
function lookupPerformanceResourceSize(targetUrl) {
if (!targetUrl || !window.performance || typeof window.performance.getEntriesByType !== 'function') {
return -1;
}
try {
var entries = window.performance.getEntriesByType('resource') || [];
var cleanTarget = targetUrl.split('#')[0];
for (var i = entries.length - 1; i >= 0; i--) {
var r = entries[i];
if (!r || !r.name) continue;
if (r.name === targetUrl || r.name === cleanTarget || r.name.indexOf(cleanTarget) !== -1) {
var size = r.decodedBodySize || r.encodedBodySize || r.transferSize || 0;
if (size > 1024) {
return size;
}
}
}
} catch (e) {}
return -1;
}
function inferQualityLabel(url, sourceEl, index) {
if (sourceEl) {
var explicit = sourceEl.getAttribute('label') ||
sourceEl.getAttribute('res') ||
sourceEl.getAttribute('size') ||
sourceEl.getAttribute('data-res') ||
sourceEl.getAttribute('data-quality') ||
sourceEl.getAttribute('title');
if (explicit) {
return /^\d+$/.test(explicit.trim()) ? (explicit.trim() + 'p') : explicit.trim();
}
}
if (url) {
var match = url.match(/(?:^|[^0-9])(2160p|1440p|1080p|720p|540p|480p|360p|240p|144p|4k|hd|sd)(?:[^0-9a-z]|$)/i);
if (match && match[1]) {
return match[1].toUpperCase();
}
var dimMatch = url.match(/(\d{3,4})x(\d{3,4})/i);
if (dimMatch && dimMatch[2]) {
return dimMatch[2] + 'p';
}
if (/\.mp4(\?|#|$)/i.test(url)) {
return index === 0 ? 'Direct MP4 (Default)' : ('MP4 Stream #' + (index + 1));
}
if (/\.ogg|ogv(\?|#|$)/i.test(url)) {
return 'OGG Video';
}
if (/\.webm(\?|#|$)/i.test(url)) {
return 'WebM Video';
}
}
return index === 0 ? 'Direct Video Stream' : ('Stream #' + (index + 1));
}
function classifyVideoUrl(url, mimeType) {
var lowerUrl = (url || '').toLowerCase();
var lowerMime = (mimeType || '').toLowerCase();
var isBlobOrMediaSource = lowerUrl.indexOf('blob:') === 0 || lowerUrl.indexOf('mediasource:') === 0;
var isHlsOrDash = STREAMING_VIDEO_REGEX.test(lowerUrl) ||
lowerMime.indexOf('mpegurl') !== -1 ||
lowerMime.indexOf('dash+xml') !== -1;
var isProtectedSite = PROTECTED_DOMAIN_REGEX.test(lowerUrl) ||
PROTECTED_DOMAIN_REGEX.test(window.location.hostname || '');
var isStreaming = isBlobOrMediaSource || isHlsOrDash || isProtectedSite;
var isDirectMp4 = !isStreaming && (lowerUrl.indexOf('http://') === 0 || lowerUrl.indexOf('https://') === 0);
return {
isStreaming: isStreaming,
isDirectMp4: isDirectMp4,
isPreferredMp4: !isStreaming && (MP4_PRIORITY_REGEX.test(lowerUrl) || lowerMime.indexOf('mp4') !== -1)
};
}
function captureFramePoster(videoEl) {
if (!videoEl) return '';
var rawPoster = videoEl.getAttribute('poster') || videoEl.poster;
if (rawPoster) {
return toAbsoluteUrl(rawPoster, videoEl.ownerDocument);
}
try {
if (videoEl.videoWidth > 0 && videoEl.videoHeight > 0 && videoEl.readyState >= 2) {
var canvas = document.createElement('canvas');
var targetW = Math.min(480, videoEl.videoWidth);
var targetH = Math.round((targetW / videoEl.videoWidth) * videoEl.videoHeight);
canvas.width = targetW;
canvas.height = targetH;
var ctx = canvas.getContext('2d');
ctx.drawImage(videoEl, 0, 0, targetW, targetH);
var dataUrl = canvas.toDataURL('image/jpeg', 0.72);
if (dataUrl && dataUrl.length > 128) {
return dataUrl;
}
}
} catch (err) {
}
return '';
}
function extractFromVideoElement(videoEl, isInsideIframe) {
if (!videoEl) return null;
var doc = videoEl.ownerDocument || document;
var rawCandidates = [];
var seenUrls = {};
function addCandidate(rawUrl, mimeType, sourceEl) {
var abs = toAbsoluteUrl(rawUrl, doc);
if (!abs || seenUrls[abs]) return;
if (abs === window.location.href && (!rawUrl || rawUrl.trim() === '')) return;
seenUrls[abs] = true;
var classification = classifyVideoUrl(abs, mimeType);
var perfBytes = lookupPerformanceResourceSize(abs);
rawCandidates.push({
url: abs,
label: inferQualityLabel(abs, sourceEl, rawCandidates.length),
mimeType: mimeType || (MP4_PRIORITY_REGEX.test(abs) ? 'video/mp4' : 'video/mp4'),
isDirectMp4: classification.isDirectMp4,
isStreaming: classification.isStreaming,
isPreferredMp4: classification.isPreferredMp4,
estimatedBytes: perfBytes
});
}
var sources = videoEl.querySelectorAll('source');
for (var i = 0; i < sources.length; i++) {
var s = sources[i];
var sUrl = s.getAttribute('src') || s.src || s.getAttribute('data-src') || s.getAttribute('data-url');
var sType = s.getAttribute('type') || s.type || '';
if (sUrl) {
addCandidate(sUrl, sType, s);
}
}
var attrSrc = videoEl.getAttribute('src');
if (attrSrc) {
addCandidate(attrSrc, videoEl.getAttribute('type') || 'video/mp4', null);
}
if (videoEl.currentSrc) {
addCandidate(videoEl.currentSrc, videoEl.getAttribute('type') || 'video/mp4', null);
}
if (videoEl.src && videoEl.src !== window.location.href) {
addCandidate(videoEl.src, videoEl.getAttribute('type') || 'video/mp4', null);
}
var dataAttrs = ['data-src', 'data-video', 'data-mp4', 'data-hd-src', 'data-sd-src', 'data-url', 'data-file'];
for (var d = 0; d < dataAttrs.length; d++) {
var val = videoEl.getAttribute(dataAttrs[d]);
if (val) {
addCandidate(val, 'video/mp4', null);
}
}
var container = videoEl.closest ? (videoEl.closest('div, section, article, figure, main') || doc.body) : doc.body;
if (container) {
var nearbyLinks = container.querySelectorAll('a[href]');
for (var j = 0; j < Math.min(nearbyLinks.length, 30); j++) {
var link = nearbyLinks[j];
var href = link.getAttribute('href') || '';
if (DIRECT_VIDEO_REGEX.test(href)) {
var absLink = toAbsoluteUrl(href, doc);
if (absLink && !seenUrls[absLink]) {
seenUrls[absLink] = true;
var linkText = (link.textContent || '').trim();
var label = (linkText && linkText.length <= 28) ? linkText : inferQualityLabel(absLink, link, rawCandidates.length);
rawCandidates.push({
url: absLink,
label: label,
mimeType: 'video/mp4',
isDirectMp4: true,
isStreaming: false,
isPreferredMp4: MP4_PRIORITY_REGEX.test(absLink),
estimatedBytes: lookupPerformanceResourceSize(absLink)
});
}
}
}
}
rawCandidates.sort(function (a, b) {
if (a.isPreferredMp4 && !b.isPreferredMp4) return -1;
if (!a.isPreferredMp4 && b.isPreferredMp4) return 1;
if (a.isDirectMp4 && !b.isDirectMp4) return -1;
if (!a.isDirectMp4 && b.isDirectMp4) return 1;
return 0;
});
var primaryUrl = rawCandidates.length > 0 ? rawCandidates[0].url : '';
var isProtectedPage = PROTECTED_DOMAIN_REGEX.test(window.location.hostname || '');
if (!primaryUrl) {
primaryUrl = 'blob:' + window.location.origin + '/protected-stream';
rawCandidates.push({
url: primaryUrl,
label: 'Protected Stream',
mimeType: 'application/x-mpegURL',
isDirectMp4: false,
isStreaming: true,
estimatedBytes: -1
});
} else if (isProtectedPage) {
for (var k = 0; k < rawCandidates.length; k++) {
rawCandidates[k].isStreaming = true;
rawCandidates[k].isDirectMp4 = false;
}
}
var dims = (videoEl.videoWidth && videoEl.videoHeight)
? (videoEl.videoWidth + 'x' + videoEl.videoHeight)
: '';
var dur = (videoEl.duration && isFinite(videoEl.duration) && videoEl.duration > 0)
? Math.round(videoEl.duration)
: 0;
return {
pageTitle: document.title || window.location.hostname || 'video',
pageUrl: window.location.href,
primaryUrl: primaryUrl,
posterUrl: captureFramePoster(videoEl),
qualities: rawCandidates,
isIframe: !!isInsideIframe,
dimensions: dims,
durationSeconds: dur
};
}
function extractFromAnchorUrl(absUrl, anchorEl) {
var classification = classifyVideoUrl(absUrl, 'video/mp4');
var label = anchorEl && anchorEl.textContent ? anchorEl.textContent.trim().slice(0, 30) : '';
if (!label) {
label = inferQualityLabel(absUrl, anchorEl, 0);
}
return {
pageTitle: document.title || window.location.hostname || 'video',
pageUrl: window.location.href,
primaryUrl: absUrl,
posterUrl: '',
qualities: [
{
url: absUrl,
label: label || 'Direct MP4',
mimeType: 'video/mp4',
isDirectMp4: classification.isDirectMp4,
isStreaming: classification.isStreaming,
estimatedBytes: lookupPerformanceResourceSize(absUrl)
}
],
isIframe: false,
dimensions: '',
durationSeconds: 0
};
}
function findVideoAtPointInDocument(doc, clientX, clientY, isIframeDoc) {
if (!doc) return null;
if (typeof doc.elementsFromPoint === 'function' && (clientX > 0 || clientY > 0)) {
var els = doc.elementsFromPoint(clientX, clientY);
for (var i = 0; i < els.length; i++) {
var el = els[i];
if (!el || !el.tagName) continue;
var tag = el.tagName.toUpperCase();
if (tag === 'VIDEO') {
return extractFromVideoElement(el, isIframeDoc);
}
var closestVideo = el.closest ? el.closest('video') : null;
if (closestVideo) {
return extractFromVideoElement(closestVideo, isIframeDoc);
}
if (el.querySelector) {
var childVid = el.querySelector('video');
if (childVid) {
var cRect = childVid.getBoundingClientRect();
if (clientX >= cRect.left - 30 && clientX <= cRect.right + 30 &&
clientY >= cRect.top - 30 && clientY <= cRect.bottom + 30) {
return extractFromVideoElement(childVid, isIframeDoc);
}
}
}
var closestAnchor = el.closest ? el.closest('a[href]') : null;
if (closestAnchor) {
var href = toAbsoluteUrl(closestAnchor.getAttribute('href'), doc);
if (DIRECT_VIDEO_REGEX.test(href) || STREAMING_VIDEO_REGEX.test(href) || /content-type=video|format=mp4/i.test(href)) {
return extractFromAnchorUrl(href, closestAnchor);
}
}
}
}
var videos = doc.querySelectorAll('video');
for (var v = 0; v < videos.length; v++) {
var video = videos[v];
var rect = video.getBoundingClientRect();
if (rect.width > 8 && rect.height > 8 &&
clientX >= rect.left - 28 && clientX <= rect.right + 28 &&
clientY >= rect.top - 28 && clientY <= rect.bottom + 28) {
return extractFromVideoElement(video, isIframeDoc);
}
}
var iframes = doc.querySelectorAll('iframe');
for (var f = 0; f < iframes.length; f++) {
var iframe = iframes[f];
var iRect = iframe.getBoundingClientRect();
if (iRect.width > 8 && iRect.height > 8 &&
clientX >= iRect.left - 16 && clientX <= iRect.right + 16 &&
clientY >= iRect.top - 16 && clientY <= iRect.bottom + 16) {
try {
var innerDoc = iframe.contentDocument || (iframe.contentWindow && iframe.contentWindow.document);
if (innerDoc) {
var localX = clientX - iRect.left;
var localY = clientY - iRect.top;
var innerHit = findVideoAtPointInDocument(innerDoc, localX, localY, true);
if (innerHit) return innerHit;
var anyInnerVideo = innerDoc.querySelector('video');
if (anyInnerVideo) {
return extractFromVideoElement(anyInnerVideo, true);
}
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
return {
pageTitle: document.title || 'Embedded Video',
pageUrl: window.location.href,
primaryUrl: 'blob:' + iframeSrc,
posterUrl: '',
qualities: [{
url: 'blob:' + iframeSrc,
label: 'Protected Iframe Stream',
mimeType: 'application/x-mpegURL',
isDirectMp4: false,
isStreaming: true,
estimatedBytes: -1
}],
isIframe: true,
dimensions: '',
durationSeconds: 0
};
}
}
}
}
return null;
}
function dispatchToAndroid(payload) {
if (!payload || !payload.primaryUrl) return false;
var now = Date.now();
if (now - lastTriggerTimestamp < 750) {
return true;
}
lastTriggerTimestamp = now;
try {
if (window.NovaVideoBridge && typeof window.NovaVideoBridge.onVideoLongPressed === 'function') {
window.NovaVideoBridge.onVideoLongPressed(JSON.stringify(payload));
return true;
}
} catch (e) {
console.error('NovaVideoBridge error:', e);
}
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
return tryCreds('same-origin')
.catch(function () { return tryCreds('include'); })
.catch(function () { return tryCreds('omit'); });
}
window.__novaProbeVideoSizeInWebView = function (videoUrl) {
if (!videoUrl || videoUrl.indexOf('blob:') === 0) return;
var perfSize = lookupPerformanceResourceSize(videoUrl);
if (perfSize > 1024 && window.NovaVideoBridge && window.NovaVideoBridge.onVideoSizeProbed) {
window.NovaVideoBridge.onVideoSizeProbed(JSON.stringify({
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
if (totalSize > 0 && window.NovaVideoBridge && window.NovaVideoBridge.onVideoSizeProbed) {
window.NovaVideoBridge.onVideoSizeProbed(JSON.stringify({
url: videoUrl,
contentLength: totalSize,
contentType: cType.split(';')[0].trim()
}));
}
}).catch(function () {
var fallbackPerf = lookupPerformanceResourceSize(videoUrl);
if (fallbackPerf > 1024 && window.NovaVideoBridge && window.NovaVideoBridge.onVideoSizeProbed) {
window.NovaVideoBridge.onVideoSizeProbed(JSON.stringify({
url: videoUrl,
contentLength: fallbackPerf,
contentType: 'video/mp4'
}));
}
});
};
function uint8ToBase64(uint8) {
var CHUNK_SZ = 0x8000;
var c = [];
for (var i = 0; i < uint8.length; i += CHUNK_SZ) {
c.push(String.fromCharCode.apply(null, uint8.subarray(i, i + CHUNK_SZ)));
}
return btoa(c.join(''));
}
window.__novaDownloadVideoViaWebView = function (downloadId, videoUrl) {
if (!window.NovaVideoBridge) return;
fetchVideoWithCredentialsFallback(videoUrl, {
'Accept': 'video/mp4,video/*;q=0.9,*/*;q=0.8'
}, null).then(function (response) {
var totalBytes = parseInt(response.headers.get('Content-Length') || '-1', 10);
var cRange = response.headers.get('Content-Range') || '';
if (totalBytes <= 0 && cRange.indexOf('/') !== -1) {
var parsedTotal = parseInt(cRange.split('/').pop().trim(), 10);
if (!isNaN(parsedTotal) && parsedTotal > 0) totalBytes = parsedTotal;
}
if (totalBytes <= 0) {
totalBytes = lookupPerformanceResourceSize(videoUrl);
}
if (!response.body || typeof response.body.getReader !== 'function') {
return response.arrayBuffer().then(function (buf) {
var bytes = new Uint8Array(buf);
var step = 98304; // 96 KB chunks
for (var offset = 0; offset < bytes.length; offset += step) {
var slice = bytes.subarray(offset, Math.min(bytes.length, offset + step));
window.NovaVideoBridge.onWebViewDownloadChunk(
downloadId,
uint8ToBase64(slice),
Math.min(bytes.length, offset + step),
bytes.length
);
}
window.NovaVideoBridge.onWebViewDownloadFinished(downloadId, bytes.length);
});
}
var reader = response.body.getReader();
var loaded = 0;
function pump() {
return reader.read().then(function (res) {
if (res.done) {
window.NovaVideoBridge.onWebViewDownloadFinished(downloadId, loaded);
return;
}
var val = res.value;
if (val && val.length > 0) {
loaded += val.length;
var step = 98304;
for (var offset = 0; offset < val.length; offset += step) {
var slice = val.subarray(offset, Math.min(val.length, offset + step));
window.NovaVideoBridge.onWebViewDownloadChunk(
downloadId,
uint8ToBase64(slice),
loaded,
totalBytes > 0 ? totalBytes : -1
);
}
}
return pump();
});
}
return pump();
}).catch(function (err) {
if (window.NovaVideoBridge && window.NovaVideoBridge.onWebViewDownloadError) {
window.NovaVideoBridge.onWebViewDownloadError(downloadId, String(err && err.message ? err.message : err));
}
});
};
function bindDirectVideoListeners(videoEl, isIframeDoc) {
if (!videoEl || videoEl.__novaDirectBound) return;
videoEl.__novaDirectBound = true;
videoEl.style.webkitTouchCallout = 'none';
videoEl.style.webkitUserSelect = 'none';
var localTimer = null;
var startX = 0;
var startY = 0;
videoEl.addEventListener('contextmenu', function (e) {
e.preventDefault();
e.stopPropagation();
var info = extractFromVideoElement(videoEl, isIframeDoc);
if (info) dispatchToAndroid(info);
return false;
}, true);
videoEl.addEventListener('touchstart', function (e) {
if (!e.touches || e.touches.length !== 1) return;
startX = e.touches[0].clientX;
startY = e.touches[0].clientY;
lastTouchX = startX;
lastTouchY = startY;
if (localTimer) clearTimeout(localTimer);
localTimer = setTimeout(function () {
localTimer = null;
var info = extractFromVideoElement(videoEl, isIframeDoc);
if (info) dispatchToAndroid(info);
}, TOUCH_HOLD_MS);
}, { capture: true, passive: true });
videoEl.addEventListener('touchmove', function (e) {
if (!e.touches || e.touches.length === 0) return;
var dx = Math.abs(e.touches[0].clientX - startX);
var dy = Math.abs(e.touches[0].clientY - startY);
if (dx > TOUCH_SLOP_PX || dy > TOUCH_SLOP_PX) {
if (localTimer) {
clearTimeout(localTimer);
localTimer = null;
}
}
}, { capture: true, passive: true });
var clearLocal = function () {
if (localTimer) {
clearTimeout(localTimer);
localTimer = null;
}
};
videoEl.addEventListener('touchend', clearLocal, { capture: true, passive: true });
videoEl.addEventListener('touchcancel', clearLocal, { capture: true, passive: true });
}
function attachListenersToDocument(doc, isIframeDoc) {
if (!doc || doc.__novaListenersAttached) return;
doc.__novaListenersAttached = true;
doc.addEventListener('contextmenu', function (e) {
var x = e.clientX || lastTouchX;
var y = e.clientY || lastTouchY;
var hit = findVideoAtPointInDocument(doc, x, y, isIframeDoc);
if (!hit && e.target) {
var targetVideo = (e.target.tagName && e.target.tagName.toUpperCase() === 'VIDEO')
? e.target
: (e.target.closest ? e.target.closest('video') : null);
if (!targetVideo && e.target.querySelector) {
targetVideo = e.target.querySelector('video');
}
if (targetVideo) {
hit = extractFromVideoElement(targetVideo, isIframeDoc);
}
}
if (hit) {
e.preventDefault();
e.stopPropagation();
dispatchToAndroid(hit);
return false;
}
}, true);
doc.addEventListener('touchstart', function (e) {
if (!e.touches || e.touches.length !== 1) {
if (touchTimer) {
clearTimeout(touchTimer);
touchTimer = null;
}
return;
}
var touch = e.touches[0];
touchStartX = touch.clientX;
touchStartY = touch.clientY;
lastTouchX = touch.clientX;
lastTouchY = touch.clientY;
if (touchTimer) clearTimeout(touchTimer);
touchTimer = setTimeout(function () {
touchTimer = null;
var hit = findVideoAtPointInDocument(doc, touchStartX, touchStartY, isIframeDoc);
if (hit) {
dispatchToAndroid(hit);
}
}, TOUCH_HOLD_MS);
}, { capture: true, passive: true });
doc.addEventListener('touchmove', function (e) {
if (!e.touches || e.touches.length === 0) return;
var touch = e.touches[0];
lastTouchX = touch.clientX;
lastTouchY = touch.clientY;
var dx = Math.abs(touch.clientX - touchStartX);
var dy = Math.abs(touch.clientY - touchStartY);
if (dx > TOUCH_SLOP_PX || dy > TOUCH_SLOP_PX) {
if (touchTimer) {
clearTimeout(touchTimer);
touchTimer = null;
}
}
}, { capture: true, passive: true });
var clearTouchTimer = function () {
if (touchTimer) {
clearTimeout(touchTimer);
touchTimer = null;
}
};
doc.addEventListener('touchend', clearTouchTimer, { capture: true, passive: true });
doc.addEventListener('touchcancel', clearTouchTimer, { capture: true, passive: true });
}
function enhanceAllVideosInDocument(doc, isIframeDoc) {
if (!doc) return;
attachListenersToDocument(doc, isIframeDoc);
var videos = doc.querySelectorAll('video');
for (var i = 0; i < videos.length; i++) {
bindDirectVideoListeners(videos[i], isIframeDoc);
}
var iframes = doc.querySelectorAll('iframe');
for (var j = 0; j < iframes.length; j++) {
try {
var innerDoc = iframes[j].contentDocument || (iframes[j].contentWindow && iframes[j].contentWindow.document);
if (innerDoc) {
enhanceAllVideosInDocument(innerDoc, true);
}
} catch (err) {}
}
}
window.__novaInspectVideoAtLastTouch = function () {
var hit = findVideoAtPointInDocument(document, lastTouchX, lastTouchY, false);
if (!hit) {
var videos = document.querySelectorAll('video');
for (var i = 0; i < videos.length; i++) {
var r = videos[i].getBoundingClientRect();
if (r.width > 20 && r.height > 20 && r.bottom > 0 && r.top < window.innerHeight) {
hit = extractFromVideoElement(videos[i], false);
if (hit) break;
}
}
}
if (hit) {
dispatchToAndroid(hit);
return JSON.stringify(hit);
}
return '';
};
window.__novaScanAllPageVideos = function () {
var results = [];
var videos = document.querySelectorAll('video');
for (var i = 0; i < videos.length; i++) {
var info = extractFromVideoElement(videos[i], false);
if (info) results.push(info);
}
return JSON.stringify(results);
};
enhanceAllVideosInDocument(document, false);
if (!window.__novaMutationObserverStarted && typeof MutationObserver !== 'undefined') {
window.__novaMutationObserverStarted = true;
var observer = new MutationObserver(function () {
enhanceAllVideosInDocument(document, false);
});
if (document.documentElement) {
observer.observe(document.documentElement, {
childList: true,
subtree: true
});
}
}
})();
