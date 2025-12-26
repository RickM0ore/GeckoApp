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

function enablePreRequestCache() {
    browser.webRequest.onBeforeRequest.addListener(
        function(details) {
            // 过滤掉已经是 content:// 的请求，防止死循环
            if (details.url.startsWith("content://")) {
                return {};
            }

            // 将原始 URL 编码，拼接到 content provider 路径后
            // 假设你的 Authority 是 com.example.myapp.glideprovider
            const safeUrl = encodeURIComponent(details.url);
            const redirectUrl = `content://re.rickmoo.gecko.infra.GlideContentProvider?url=${safeUrl}`;

            console.log("Redirecting " + details.url + " to " + redirectUrl);

            return { redirectUrl: redirectUrl };
        },
        { urls: ["<all_urls>"], types: ["image"] }, // 只拦截图片
        ["blocking"] // 必须是阻塞模式才能重定向
    );
}