package net.onefivefour.echolist.core.networking.data.client

interface ConnectRpcClient {
    suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res>
}