package net.onefivefour.echolist.data.network.logging

class NetworkLoggingConfig {
    var minLogLevel: LogLevel = LogLevel.DEBUG
    var logSink: (String) -> Unit = ::println
}
