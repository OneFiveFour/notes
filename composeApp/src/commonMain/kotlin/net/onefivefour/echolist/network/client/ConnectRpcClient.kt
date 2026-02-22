package net.onefivefour.echolist.network.client

interface ConnectRpcClient {
    suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res>
}
