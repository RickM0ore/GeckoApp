
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