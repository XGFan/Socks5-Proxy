import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;

import java.net.InetSocketAddress;

public class DNSClient {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        final ReceiveHandler receiveHandler = new ReceiveHandler();

        final Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .option(ChannelOption.SO_BROADCAST, true)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new DatagramDnsQueryEncoder())
                                .addLast(new DatagramDnsResponseDecoder())
                                .addLast(receiveHandler);
                    }
                });
        ChannelFuture sync = bootstrap.bind(0).sync();
        final DefaultDnsQuestion defaultDnsQuestion = new DefaultDnsQuestion("test4x.com", DnsRecordType.A);
        final DatagramDnsQuery datagramDnsQuery = new DatagramDnsQuery(null, new InetSocketAddress("114.114.114.114", 53), 1);
        datagramDnsQuery.setRecord(DnsSection.QUESTION, defaultDnsQuestion);
        final ChannelFuture sync1 = sync.channel().writeAndFlush(datagramDnsQuery).sync();
    }

    @ChannelHandler.Sharable
    static class ReceiveHandler extends SimpleChannelInboundHandler<DatagramDnsResponse> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) throws Exception {
            System.out.println(msg);
        }
    }
}
