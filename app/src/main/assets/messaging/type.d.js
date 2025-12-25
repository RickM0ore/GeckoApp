/**
 * @type {(func: Function, targetScope: Object, options?: {defineAs?: string, allowCallbacks?: boolean}) => void}
 */
// @ts-ignore (这一行是为了防止 TS 检查器报错说 window 上没有这个属性)
exportFunction
/**
 * @type {<T>(obj: T, targetScope: Object, options?: {cloneFunctions?: boolean}) => T}
 */
// @ts-ignore
cloneInto

class NativeMessage {
    constructor(type, extensionName, action, data) {
        this.type = type
        this.extensionName = extensionName
        this.payload = {action, data}
    }
}

class OneTimeNativeMessage extends NativeMessage {
    constructor(extensionName, action, data) {
        super('CALL_NATIVE_ONE_TIME', extensionName, action, data);
    }
}

class StreamNativeMessage extends NativeMessage {
    constructor(extensionName, action, data) {
        super('CALL_NATIVE_CONNECTION', extensionName, action, data);
    }
}