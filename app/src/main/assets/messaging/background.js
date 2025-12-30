/**
 * @param message {NativeMessage}
 * @param sender {MessageSender}
 * @param sendResponse {(any)=>void}
 * @return {boolean}
 * */
const listener = function (message, sender, sendResponse) {
    switch (message.type) {
        case 'CALL_NATIVE_ONE_TIME': {
            let sending = browser.runtime.sendNativeMessage(message.extensionName, message.payload)
            sending.then(sendResponse)
            return true
        }
        case 'CALL_NATIVE_CONNECTION': {
            let port = browser.runtime.connectNative(message.extensionName)
            port.onMessage.addListener((m) => {
                sendResponse(m);
            })
            port.postMessage(message.payload)
            return true
        }
    }
}
browser.runtime.onMessage.addListener(listener);

function enablePreRequestCache() {
    browser.webRequest.onHeadersReceived.addListener(
        function (details) {
            const cleanHeaders = details.responseHeaders.filter(header => {
                const name = header.name.toLowerCase();
                return !['pragma',].includes(name) || !(name === 'cache-control' && header.value.toLowerCase() === 'no-store') ||
                    !('expires' === name && header.value.toLowerCase() === '0');
            });

            if (!cleanHeaders.some((header) => {
                return header.name.toLowerCase() === 'cache-control'
            }))
                cleanHeaders.push({
                    name: "Cache-Control",
                    value: "public, max-age=1209600, immutable" //2 weeks
                });

            return {responseHeaders: cleanHeaders};
        },
        {urls: ["<all_urls>"], types: ["image"]},
        ["blocking", "responseHeaders"]
    );
}