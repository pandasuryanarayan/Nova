/**
 * Nova Browser - Real Desktop-Grade DevTools Inspector Engine
 * Powers the 4 DevTools tabs: Elements, Sources, Console, and Network.
 */
(function () {
  if (window.__novaDevToolsInstalled) {
    return;
  }
  window.__novaDevToolsInstalled = true;

  var nodeCounter = 1;
  var inspectPickerActive = false;
  var selectedNodeId = null;
  var overlayEl = null;
  var tooltipEl = null;
  var reportedResourceNames = {};

  function getOrAssignNodeId(el) {
    if (!el || el.nodeType !== 1) return '';
    var existing = el.getAttribute('data-nova-id');
    if (existing) return existing;
    var assigned = 'nova-node-' + (nodeCounter++);
    el.setAttribute('data-nova-id', assigned);
    return assigned;
  }

  function ensureOverlayElements() {
    if (!document.body) return;
    if (!overlayEl || !document.body.contains(overlayEl)) {
      overlayEl = document.createElement('div');
      overlayEl.id = '__nova_devtools_overlay__';
      overlayEl.style.cssText = [
        'position:fixed',
        'pointer-events:none',
        'z-index:2147483646',
        'border:2px solid #00E5FF',
        'background:rgba(0, 229, 255, 0.18)',
        'box-shadow:0 0 0 1px rgba(124,77,255,0.6)',
        'border-radius:3px',
        'transition:all 0.08s ease',
        'display:none'
      ].join(';');
      document.body.appendChild(overlayEl);
    }
    if (!tooltipEl || !document.body.contains(tooltipEl)) {
      tooltipEl = document.createElement('div');
      tooltipEl.id = '__nova_devtools_tooltip__';
      tooltipEl.style.cssText = [
        'position:fixed',
        'pointer-events:none',
        'z-index:2147483647',
        'background:#0B1021',
        'color:#00E5FF',
        'border:1px solid #00E5FF',
        'padding:4px 8px',
        'border-radius:6px',
        'font-family:monospace',
        'font-size:11px',
        'font-weight:bold',
        'white-space:nowrap',
        'box-shadow:0 4px 12px rgba(0,0,0,0.5)',
        'display:none'
      ].join(';');
      document.body.appendChild(tooltipEl);
    }
  }

  function buildElementSelectorLabel(el) {
    if (!el || !el.tagName) return 'element';
    var label = el.tagName.toLowerCase();
    if (el.id && el.id.indexOf('__nova_') !== 0) {
      label += '#' + el.id;
    }
    if (el.className && typeof el.className === 'string') {
      var classes = el.className.trim().split(/\s+/).filter(Boolean).slice(0, 2);
      if (classes.length > 0) {
        label += '.' + classes.join('.');
      }
    }
    return label;
  }

  function highlightElementOnPage(el, autoHideMs) {
    ensureOverlayElements();
    if (!el || !overlayEl || !tooltipEl) return;
    var rect = el.getBoundingClientRect();
    overlayEl.style.display = 'block';
    overlayEl.style.left = rect.left + 'px';
    overlayEl.style.top = rect.top + 'px';
    overlayEl.style.width = Math.max(4, rect.width) + 'px';
    overlayEl.style.height = Math.max(4, rect.height) + 'px';

    tooltipEl.style.display = 'block';
    tooltipEl.textContent = buildElementSelectorLabel(el) + ' • ' + Math.round(rect.width) + '×' + Math.round(rect.height) + 'px';
    var topPos = rect.top - 28;
    if (topPos < 6) topPos = rect.bottom + 6;
    var leftPos = Math.max(6, Math.min(window.innerWidth - 220, rect.left));
    tooltipEl.style.top = topPos + 'px';
    tooltipEl.style.left = leftPos + 'px';

    if (autoHideMs && autoHideMs > 0) {
      setTimeout(function () {
        if (!inspectPickerActive && overlayEl && tooltipEl) {
          overlayEl.style.display = 'none';
          tooltipEl.style.display = 'none';
        }
      }, autoHideMs);
    }
  }

  function hideHighlightOverlay() {
    if (overlayEl) overlayEl.style.display = 'none';
    if (tooltipEl) tooltipEl.style.display = 'none';
  }

  function inspectDomElementDetail(el) {
    if (!el || el.nodeType !== 1) return null;
    var nodeId = getOrAssignNodeId(el);
    selectedNodeId = nodeId;
    var rect = el.getBoundingClientRect();
    var computed = window.getComputedStyle ? window.getComputedStyle(el) : null;

    var attrs = [];
    if (el.attributes) {
      for (var i = 0; i < el.attributes.length; i++) {
        var a = el.attributes[i];
        if (a.name !== 'data-nova-id') {
          attrs.push({ name: a.name, value: a.value });
        }
      }
    }

    var styleProps = [
      'display', 'position', 'width', 'height', 'color', 'background-color',
      'font-family', 'font-size', 'font-weight', 'margin', 'padding',
      'border', 'border-radius', 'flex-direction', 'justify-content',
      'align-items', 'z-index', 'opacity', 'overflow'
    ];
    var computedList = [];
    if (computed) {
      for (var s = 0; s < styleProps.length; s++) {
        var prop = styleProps[s];
        var val = computed.getPropertyValue(prop);
        if (val) {
          computedList.push({ property: prop, value: val });
        }
      }
    }

    var breadcrumbs = [];
    var curr = el;
    while (curr && curr.nodeType === 1 && breadcrumbs.length < 8) {
      breadcrumbs.unshift(buildElementSelectorLabel(curr));
      curr = curr.parentElement;
    }

    var cleanOuterHtml = (el.outerHTML || '').replace(/\s*data-nova-id="[^"]*"/g, '');
    if (cleanOuterHtml.length > 4000) {
      cleanOuterHtml = cleanOuterHtml.slice(0, 4000) + '\n<!-- truncated -->';
    }

    return {
      nodeId: nodeId,
      tagName: el.tagName.toLowerCase(),
      selector: buildElementSelectorLabel(el),
      breadcrumb: breadcrumbs.join(' > '),
      id: el.id || '',
      className: (typeof el.className === 'string') ? el.className : '',
      inlineStyle: el.getAttribute('style') || '',
      innerText: (el.innerText || el.textContent || '').trim().slice(0, 500),
      innerHtml: (el.innerHTML || '').slice(0, 2500),
      outerHtml: cleanOuterHtml,
      boxMetrics: {
        width: Math.round(rect.width),
        height: Math.round(rect.height),
        top: Math.round(rect.top),
        left: Math.round(rect.left)
      },
      attributes: attrs,
      computedStyles: computedList
    };
  }

  // --- 1. ELEMENTS TAB API ---

  window.__novaDevToolsSetInspectMode = function (enabled) {
    inspectPickerActive = !!enabled;
    if (!inspectPickerActive) {
      hideHighlightOverlay();
    }
    return inspectPickerActive;
  };

  window.__novaDevToolsGetDomTree = function () {
    var nodes = [];
    function walk(el, depth) {
      if (!el || el.nodeType !== 1 || depth > 9 || nodes.length >= 260) return;
      if (el.id === '__nova_devtools_overlay__' || el.id === '__nova_devtools_tooltip__') return;
      var nodeId = getOrAssignNodeId(el);
      var textPreview = '';
      for (var c = 0; c < el.childNodes.length; c++) {
        if (el.childNodes[c].nodeType === 3) {
          var t = (el.childNodes[c].nodeValue || '').trim();
          if (t) {
            textPreview = t.slice(0, 60);
            break;
          }
        }
      }
      nodes.push({
        nodeId: nodeId,
        depth: depth,
        tagName: el.tagName.toLowerCase(),
        selector: buildElementSelectorLabel(el),
        childCount: el.children ? el.children.length : 0,
        textPreview: textPreview
      });
      if (el.children) {
        for (var i = 0; i < el.children.length; i++) {
          walk(el.children[i], depth + 1);
        }
      }
    }
    if (document.documentElement) {
      walk(document.documentElement, 0);
    }
    return JSON.stringify(nodes);
  };

  window.__novaDevToolsSelectNodeById = function (nodeId) {
    var el = document.querySelector('[data-nova-id="' + nodeId + '"]');
    if (!el) return '';
    try {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    } catch (e) {}
    highlightElementOnPage(el, 2500);
    var detail = inspectDomElementDetail(el);
    if (detail && window.NovaDevToolsBridge && window.NovaDevToolsBridge.onElementSelected) {
      var json = JSON.stringify(detail);
      window.NovaDevToolsBridge.onElementSelected(json);
      return json;
    }
    return '';
  };

  window.__novaDevToolsApplyElementEdit = function (nodeId, newInlineStyle, newTextContent, attrName, attrValue) {
    var el = document.querySelector('[data-nova-id="' + nodeId + '"]');
    if (!el) return '';
    if (typeof newInlineStyle === 'string') {
      el.setAttribute('style', newInlineStyle);
    }
    if (typeof newTextContent === 'string' && el.children.length === 0) {
      el.textContent = newTextContent;
    }
    if (attrName && typeof attrName === 'string' && attrName.trim() !== '') {
      el.setAttribute(attrName.trim(), attrValue || '');
    }
    highlightElementOnPage(el, 1800);
    var updated = inspectDomElementDetail(el);
    if (updated && window.NovaDevToolsBridge && window.NovaDevToolsBridge.onElementSelected) {
      var json = JSON.stringify(updated);
      window.NovaDevToolsBridge.onElementSelected(json);
      return json;
    }
    return '';
  };

  window.__novaDevToolsDeleteNode = function (nodeId) {
    var el = document.querySelector('[data-nova-id="' + nodeId + '"]');
    if (el && el.parentElement && el.tagName.toLowerCase() !== 'html' && el.tagName.toLowerCase() !== 'body') {
      el.parentElement.removeChild(el);
      hideHighlightOverlay();
      return true;
    }
    return false;
  };

  // Touch / Click interceptor when Element Picker Mode is active
  document.addEventListener('click', function (e) {
    if (!inspectPickerActive) return;
    var target = e.target;
    if (!target || target.id === '__nova_devtools_overlay__' || target.id === '__nova_devtools_tooltip__') return;
    e.preventDefault();
    e.stopPropagation();
    highlightElementOnPage(target, 3000);
    var detail = inspectDomElementDetail(target);
    inspectPickerActive = false;
    if (detail && window.NovaDevToolsBridge && window.NovaDevToolsBridge.onElementSelected) {
      window.NovaDevToolsBridge.onElementSelected(JSON.stringify(detail));
    }
    return false;
  }, true);

  document.addEventListener('touchmove', function (e) {
    if (!inspectPickerActive || !e.touches || e.touches.length === 0) return;
    var t = e.touches[0];
    var el = document.elementFromPoint(t.clientX, t.clientY);
    if (el && el.id !== '__nova_devtools_overlay__' && el.id !== '__nova_devtools_tooltip__') {
      highlightElementOnPage(el, 0);
    }
  }, { capture: true, passive: true });

  // --- 2. SOURCES TAB API ---

  window.__novaDevToolsGetSources = function () {
    var sources = [];
    // 1. Main HTML Document
    var docHtml = document.documentElement ? document.documentElement.outerHTML.replace(/\s*data-nova-id="[^"]*"/g, '') : '';
    sources.push({
      id: 'doc-main-html',
      name: (window.location.pathname && window.location.pathname !== '/') ? window.location.pathname.split('/').pop() || 'index.html' : 'index.html (Document)',
      url: window.location.href,
      type: 'HTML',
      sizeBytes: docHtml.length,
      content: docHtml.slice(0, 35000)
    });

    // 2. Stylesheets (<link rel="stylesheet"> & <style>)
    var styles = document.querySelectorAll('style, link[rel="stylesheet"]');
    for (var i = 0; i < styles.length; i++) {
      var st = styles[i];
      if (st.tagName.toLowerCase() === 'style') {
        var cssText = st.textContent || '';
        if (cssText.trim().length > 0) {
          sources.push({
            id: 'inline-style-' + (i + 1),
            name: 'inline-style-' + (i + 1) + '.css',
            url: window.location.href + '#style-' + (i + 1),
            type: 'CSS',
            sizeBytes: cssText.length,
            content: cssText.slice(0, 30000)
          });
        }
      } else {
        var href = st.href || st.getAttribute('href') || '';
        if (href) {
          var cssRulesText = '';
          try {
            if (st.sheet && st.sheet.cssRules) {
              var rules = [];
              for (var r = 0; r < Math.min(st.sheet.cssRules.length, 300); r++) {
                rules.push(st.sheet.cssRules[r].cssText);
              }
              cssRulesText = rules.join('\n\n');
            }
          } catch (corsErr) {
            cssRulesText = '/* External stylesheet: ' + href + ' */\n/* Tap "Fetch Full Remote Source" or view via Network tab */';
          }
          sources.push({
            id: 'ext-css-' + (i + 1),
            name: href.split('/').pop().split('?')[0] || ('stylesheet-' + (i + 1) + '.css'),
            url: href,
            type: 'CSS',
            sizeBytes: cssRulesText.length,
            content: cssRulesText || ('/* External CSS URL: ' + href + ' */')
          });
        }
      }
    }

    // 3. Scripts (<script>)
    var scripts = document.querySelectorAll('script');
    for (var j = 0; j < scripts.length; j++) {
      var sc = scripts[j];
      var src = sc.src || sc.getAttribute('src') || '';
      if (src) {
        sources.push({
          id: 'ext-js-' + (j + 1),
          name: src.split('/').pop().split('?')[0] || ('script-' + (j + 1) + '.js'),
          url: src,
          type: 'JS',
          sizeBytes: 0,
          content: '// External Script: ' + src + '\n// Loading source preview...'
        });
      } else {
        var jsCode = (sc.textContent || '').trim();
        if (jsCode.length > 0 && jsCode.indexOf('__novaDevToolsInstalled') === -1 && jsCode.indexOf('__novaVideoDetectorInstalled') === -1) {
          sources.push({
            id: 'inline-js-' + (j + 1),
            name: 'inline-script-' + (j + 1) + '.js',
            url: window.location.href + '#script-' + (j + 1),
            type: 'JS',
            sizeBytes: jsCode.length,
            content: jsCode.slice(0, 30000)
          });
        }
      }
    }

    return JSON.stringify(sources);
  };

  // --- 3. CONSOLE TAB INTERCEPTOR ---

  function formatConsoleArg(arg) {
    if (arg === null) return 'null';
    if (arg === undefined) return 'undefined';
    if (typeof arg === 'string') return arg;
    if (arg instanceof Error) return arg.name + ': ' + arg.message + (arg.stack ? '\n' + arg.stack : '');
    if (typeof arg === 'object') {
      try {
        return JSON.stringify(arg, null, 2);
      } catch (e) {
        return String(arg);
      }
    }
    return String(arg);
  }

  function emitConsoleToAndroid(level, argsArray, sourceAndLine) {
    try {
      var parts = [];
      for (var i = 0; i < argsArray.length; i++) {
        parts.push(formatConsoleArg(argsArray[i]));
      }
      var payload = {
        level: level,
        message: parts.join(' '),
        source: sourceAndLine || window.location.hostname || 'page',
        timestamp: Date.now()
      };
      if (window.NovaDevToolsBridge && typeof window.NovaDevToolsBridge.onConsoleLog === 'function') {
        window.NovaDevToolsBridge.onConsoleLog(JSON.stringify(payload));
      }
    } catch (e) {}
  }

  ['log', 'info', 'warn', 'error'].forEach(function (method) {
    var orig = console[method];
    console[method] = function () {
      emitConsoleToAndroid(method.toUpperCase(), arguments, 'console.' + method);
      if (orig) {
        return orig.apply(console, arguments);
      }
    };
  });

  window.addEventListener('error', function (e) {
    var src = (e.filename ? e.filename.split('/').pop() : 'runtime') + (e.lineno ? ':' + e.lineno : '');
    emitConsoleToAndroid('ERROR', [e.message || 'Uncaught Error'], src);
  });

  window.addEventListener('unhandledrejection', function (e) {
    emitConsoleToAndroid('ERROR', ['Unhandled Promise Rejection:', e.reason], 'Promise');
  });

  // --- 4. NETWORK TAB INTERCEPTOR (PerformanceResourceTiming + fetch + XHR) ---

  function sendNetworkEntry(entryObj) {
    try {
      if (window.NovaDevToolsBridge && typeof window.NovaDevToolsBridge.onNetworkEntry === 'function') {
        window.NovaDevToolsBridge.onNetworkEntry(JSON.stringify(entryObj));
      }
    } catch (e) {}
  }

  // Hook window.fetch for real status code, method, duration, and response body snippet
  if (typeof window.fetch === 'function') {
    var origFetch = window.fetch;
    window.fetch = function (input, init) {
      var startMs = Date.now();
      var url = (typeof input === 'string') ? input : (input && input.url ? input.url : String(input));
      var method = (init && init.method) ? init.method.toUpperCase() : ((input && input.method) ? input.method.toUpperCase() : 'GET');
      return origFetch.apply(this, arguments).then(function (response) {
        var duration = Date.now() - startMs;
        var clone = response.clone();
        var contentType = response.headers.get('content-type') || 'application/octet-stream';
        var contentLen = parseInt(response.headers.get('content-length') || '0', 10) || 0;
        clone.text().then(function (bodyText) {
          sendNetworkEntry({
            url: url,
            method: method,
            status: response.status || 200,
            initiatorType: 'fetch',
            mimeType: contentType.split(';')[0],
            sizeBytes: contentLen || bodyText.length,
            durationMs: duration,
            responsePreview: bodyText.slice(0, 1500),
            timestamp: startMs
          });
        }).catch(function () {
          sendNetworkEntry({
            url: url,
            method: method,
            status: response.status || 200,
            initiatorType: 'fetch',
            mimeType: contentType.split(';')[0],
            sizeBytes: contentLen,
            durationMs: duration,
            responsePreview: '',
            timestamp: startMs
          });
        });
        return response;
      }).catch(function (err) {
        sendNetworkEntry({
          url: url,
          method: method,
          status: 0,
          initiatorType: 'fetch',
          mimeType: 'error',
          sizeBytes: 0,
          durationMs: Date.now() - startMs,
          responsePreview: String(err),
          timestamp: startMs
        });
        throw err;
      });
    };
  }

  // Hook XMLHttpRequest
  if (typeof window.XMLHttpRequest === 'function') {
    var origOpen = XMLHttpRequest.prototype.open;
    var origSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.open = function (method, url) {
      this.__novaMethod = (method || 'GET').toUpperCase();
      this.__novaUrl = String(url || '');
      return origOpen.apply(this, arguments);
    };
    XMLHttpRequest.prototype.send = function () {
      var xhr = this;
      var startMs = Date.now();
      xhr.addEventListener('loadend', function () {
        var duration = Date.now() - startMs;
        var cType = '';
        try { cType = (xhr.getResponseHeader('content-type') || '').split(';')[0]; } catch (e) {}
        var preview = '';
        try {
          if (!xhr.responseType || xhr.responseType === 'text') {
            preview = (xhr.responseText || '').slice(0, 1500);
          }
        } catch (e) {}
        sendNetworkEntry({
          url: xhr.__novaUrl || window.location.href,
          method: xhr.__novaMethod || 'GET',
          status: xhr.status || 200,
          initiatorType: 'xhr',
          mimeType: cType || 'application/json',
          sizeBytes: preview.length,
          durationMs: duration,
          responsePreview: preview,
          timestamp: startMs
        });
      });
      return origSend.apply(this, arguments);
    };
  }

  // Capture all Resource Timing entries (scripts, stylesheets, images, media, document)
  function flushPerformanceResources() {
    if (!window.performance || typeof window.performance.getEntriesByType !== 'function') return;
    var navEntries = window.performance.getEntriesByType('navigation') || [];
    for (var n = 0; n < navEntries.length; n++) {
      var nav = navEntries[n];
      var navKey = 'NAV:' + nav.name;
      if (!reportedResourceNames[navKey]) {
        reportedResourceNames[navKey] = true;
        sendNetworkEntry({
          url: nav.name || window.location.href,
          method: 'GET',
          status: nav.responseStatus || 200,
          initiatorType: 'document',
          mimeType: 'text/html',
          sizeBytes: nav.transferSize || nav.encodedBodySize || 0,
          durationMs: Math.round(nav.duration || 0),
          responsePreview: 'Main HTML Document (' + (document.title || '') + ')',
          timestamp: Date.now()
        });
      }
    }

    var resEntries = window.performance.getEntriesByType('resource') || [];
    for (var i = 0; i < resEntries.length; i++) {
      var r = resEntries[i];
      if (!r.name || reportedResourceNames[r.name]) continue;
      reportedResourceNames[r.name] = true;
      var iType = r.initiatorType || 'other';
      if (iType === 'fetch' || iType === 'xmlhttprequest') continue; // Already captured with body preview
      sendNetworkEntry({
        url: r.name,
        method: 'GET',
        status: r.responseStatus || 200,
        initiatorType: iType,
        mimeType: iType,
        sizeBytes: r.transferSize || r.encodedBodySize || 0,
        durationMs: Math.round(r.duration || 0),
        responsePreview: '',
        timestamp: Date.now()
      });
    }
  }

  flushPerformanceResources();

  if (typeof PerformanceObserver !== 'undefined') {
    try {
      var perfObs = new PerformanceObserver(function () {
        flushPerformanceResources();
      });
      perfObs.observe({ entryTypes: ['resource', 'navigation'] });
    } catch (e) {}
  }

  window.__novaDevToolsRefreshAll = function () {
    flushPerformanceResources();
    return window.__novaDevToolsGetDomTree();
  };
})();
