// ============================================
// COMPONENTS INITIALIZATION - AGGRESSIVE APPROACH
// ============================================
console.log('🔴 [INIT] components-init.js LOADED at', new Date().toISOString());


// Visible debug panel
function createDebugPanel() {
    const panel = document.createElement('div');
    panel.id = 'debug-panel';
    panel.style.cssText = `
        position: fixed;
        top: 10px;
        right: 10px;
        background: rgba(0,0,0,0.8);
        color: #00ff00;
        padding: 10px 15px;
        border-radius: 5px;
        font-family: monospace;
        font-size: 11px;
        z-index: 10000;
        max-width: 300px;
        border: 1px solid #00ff00;
    `;
    panel.innerHTML = '<div id="debug-content">LOADING COMPONENTS...</div>';
    document.body.appendChild(panel);
    return panel;
}

function updateDebugPanel(message) {
    const panel = document.getElementById('debug-panel');
    const content = document.getElementById('debug-content');
    if (content) {
        content.innerHTML += '<br>' + message;
        panel.style.maxHeight = '400px';
        panel.style.overflowY = 'auto';
    }
}

// Ensure containers exist immediately
function ensureContainers() {
    updateDebugPanel('🔴 Ensuring containers...');
    
    if (!document.getElementById('headerContainer')) {
        const div = document.createElement('div');
        div.id = 'headerContainer';
        document.body.insertBefore(div, document.body.firstChild);
        updateDebugPanel('  ✓ Created headerContainer');
    }
    
    if (!document.getElementById('pageHeaderContainer')) {
        const div = document.createElement('div');
        div.id = 'pageHeaderContainer';
        const header = document.getElementById('headerContainer');
        header.parentNode.insertBefore(div, header.nextSibling);
        updateDebugPanel('  ✓ Created pageHeaderContainer');
    }
    
    if (!document.getElementById('footerContainer')) {
        const div = document.createElement('div');
        div.id = 'footerContainer';
        document.body.appendChild(div);
        updateDebugPanel('  ✓ Created footerContainer');
    }
}

async function loadFooterContactInfo() {
    const container = document.getElementById('contact-info-container');
    if (!container) return;

    try {
        const response = await fetch('/api/public/settings/contact-info');
        const result = await response.json();

        if (result.success && result.data) {
            const contact = result.data;
            container.innerHTML = `
                <p><i class="fas fa-map-marker-alt"></i><span>${contact.address || 'N/A'}</span></p>
                <p><i class="fas fa-phone"></i><a href="tel:${(contact.phone || '').replace(/[^0-9+]/g, '')}">${contact.phone || 'N/A'}</a></p>
                <p><i class="fas fa-envelope"></i><a href="mailto:${contact.email || ''}">${contact.email || 'N/A'}</a></p>
                <p><i class="fas fa-clock"></i><span>${contact.operatingHours || 'N/A'}</span></p>
            `;
        }
    } catch (e) {
        console.error(e);
    }
}
function loadComponent(componentName, containerId) {
    updateDebugPanel(`🔴 Loading ${componentName}...`);

    const container = document.getElementById(containerId);
    if (!container) {
        updateDebugPanel(`  ❌ Container ${containerId} NOT FOUND!`);
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {

                container.innerHTML = xhr.responseText;
                updateDebugPanel(`  ✅ ${componentName} LOADED`);

                // HEADER INIT
                if (componentName === 'header' && typeof window.initHeader === 'function') {
                    window.initHeader();
                }

                // FOOTER INIT (INI YANG BENAR)
                if (componentName === 'footer') {
                    if (typeof window.loadFooterContactInfo === 'function') {
                        window.loadFooterContactInfo();
                    }
                }

            } else {
                updateDebugPanel(`  ❌ ${componentName} FAILED: ${xhr.status}`);
            }
        }
    };

    xhr.onerror = function () {
        updateDebugPanel(`  ❌ XHR Error: ${componentName}`);
    };

    xhr.open('GET', `/components/${componentName}.html`, true);
    xhr.send();
}

// Main initialization
function initializeComponents() {
    updateDebugPanel('🔴 Starting initialization...');
    ensureContainers();
    
    // Load all components
    loadComponent('header', 'headerContainer');
    loadComponent('page-header', 'pageHeaderContainer');
    loadComponent('footer', 'footerContainer');
    
    updateDebugPanel('🔴 Loading initiated');
}

// Execute immediately when DOM is ready
if (document.readyState === 'loading') {
    updateDebugPanel('Waiting for DOMContentLoaded...');
    document.addEventListener('DOMContentLoaded', function() {
        updateDebugPanel('DOMContentLoaded fired');
        initializeComponents();
    });
} else {
    updateDebugPanel('DOM ready, initializing now');
    initializeComponents();
}

// Also try initialization after a small delay to catch async issues
setTimeout(function() {
    updateDebugPanel('--- Verification check ---');
    const header = document.getElementById('headerContainer');
    const pageHeader = document.getElementById('pageHeaderContainer');
    const footer = document.getElementById('footerContainer');
    
    const h = header && header.innerHTML.length > 0 ? '✅' : '❌';
    const p = pageHeader && pageHeader.innerHTML.length > 0 ? '✅' : '❌';
    const f = footer && footer.innerHTML.length > 0 ? '✅' : '❌';
    
    updateDebugPanel(`${h} header ${h === '✅' ? '+' + header.innerHTML.length + 'b' : 'EMPTY'}`);
    updateDebugPanel(`${p} page-header ${p === '✅' ? '+' + pageHeader.innerHTML.length + 'b' : 'EMPTY'}`);
    updateDebugPanel(`${f} footer ${f === '✅' ? '+' + footer.innerHTML.length + 'b' : 'EMPTY'}`);
    
    // If still empty, try again
    if ((header && header.innerHTML.length === 0) ||
        (pageHeader && pageHeader.innerHTML.length === 0) ||
        (footer && footer.innerHTML.length === 0)) {
        updateDebugPanel('Retrying...');
        setTimeout(initializeComponents, 500);
    } else {
        updateDebugPanel('✅✅✅ ALL LOADED ✅✅✅');
    }
}, 500);

