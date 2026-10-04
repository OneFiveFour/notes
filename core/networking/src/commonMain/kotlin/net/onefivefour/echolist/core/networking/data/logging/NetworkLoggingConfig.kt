package net.onefivefour.echolist.core.networking.data.logging

class NetworkLoggingConfig {
    var minLogLevel: LogLevel = LogLevel.DEBUG
    var logSink: (String) -> Unit = ::println
}