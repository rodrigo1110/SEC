package sec.bftb.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import sec.bftb.grpc.BFTBankingGrpc;
import sec.bftb.grpc.Contract.*;

public class ServerFrontend {
    private BFTBankingGrpc.BFTBankingBlockingStub stub;
    private final ManagedChannel channel;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    /** Helper method to print debug messages. */
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    public ServerFrontend(String host, int port) {
        final String target = host + ":" + port;
        debug("Target:" + target);
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = BFTBankingGrpc.newBlockingStub(channel);
    }

    public PingResponse ping(PingRequest request) {
        return stub.ping(request);
    }

    public void channelEnd() {
        channel.shutdownNow();
    }
}
