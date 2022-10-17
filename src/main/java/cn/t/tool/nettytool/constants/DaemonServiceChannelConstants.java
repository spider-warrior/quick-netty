package cn.t.tool.nettytool.constants;

import io.netty.util.AttributeKey;

public class DaemonServiceChannelConstants {
    public static final AttributeKey<Throwable> STARTUP_ERROR = AttributeKey.newInstance("startupError");
}
