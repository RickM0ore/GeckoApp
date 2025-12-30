/**
 *
 *
 * exportFunction @type {(func: Function, targetScope: Object, options?: {defineAs?: string, allowCallbacks?: boolean}) => void}
 * cloneInto @type {<T>(obj: T, targetScope: Object, options?: {cloneFunctions?: boolean}) => T}
 *
 * @param message {NativeMessage}
 * @return {Promise<any>}
 * */
function callNative(message) {
    return new window.Promise((resolve, reject) => {
        // 执行扩展内部的异步操作
        browser.runtime.sendMessage(message).then(response => {
            // 【重要】结果数据也必须克隆到网页作用域
            // 如果 response 是对象，不 cloneInto 依然会报错或无法读取
            try {
                resolve(cloneInto(response, window, {cloneFunctions: true}));
            } catch (e) {
                // 如果是简单类型(string/int)，直接返回即可
                resolve(response);
            }
        }).catch(error => {
            // 把错误信息也克隆过去（可选，视错误对象复杂度而定）
            reject(new window.Error(error.message || "Native Bridge Error"));
        });
    });
}

const bridgeObject = {
    geckoBridge: {
        communicationTest: function (data) {
            return callNative(new OneTimeNativeMessage('geckoBridge', 'communicationTest', data))
        }
    },
    activityConfiguration: {
        setSystemUiVisible: function (visibility) {
            return callNative(new OneTimeNativeMessage('activityConfiguration', 'setSystemUiVisible', visibility))
        },
        edgeToEdge: function (statusBarStyle, navigationBarStyle) {
            return callNative(new OneTimeNativeMessage('activityConfiguration', 'edgeToEdge',
                {statusBarStyle, navigationBarStyle}));
        }
    }
}

try {
    window.wrappedJSObject.nativeBridge = cloneInto(bridgeObject, window, {cloneFunctions: true});
    console.log("Android Bridge injected successfully");
} catch (e) {
    // 降级处理（非严格安全模式下）
    window.androidBridge = callNative;
    console.error("Android Bridge inject failed");

}