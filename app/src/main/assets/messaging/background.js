/**
 * @param message {NativeMessage}
 * @param sender {MessageSender
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