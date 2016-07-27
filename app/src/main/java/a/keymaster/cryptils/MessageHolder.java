package a.keymaster.cryptils;

import android.util.Base64;

import java.util.Arrays;

// message part is a public key, 65 bytes
// signature is 64 bytes
public class MessageHolder {
    private byte[] msg_;
    private byte[] sig_;

    public MessageHolder( byte[] msg, byte[] sig ) throws Exception {
        if (null == msg || 65 != msg.length || null == sig || 0 == sig.length)
            throw new Exception( "MessageHolder(): invalid arg");

        msg_ = msg;
        sig_ = sig;
    }

    public byte[] msg() { return msg_; }
    public byte[] sig() { return sig_; }

    public static MessageHolder parse( String src ) throws Exception {
      if (null == src || 0 == src.length())
          throw new Exception( "MessageHolder.fromString(): bad arg" );

        byte[] data = Base64.decode( src, Base64.DEFAULT );
        return new MessageHolder( Arrays.copyOfRange(data, 0, 65), Arrays.copyOfRange(data,65,data.length) );
    }

    public String toString() {
        byte[] raw = ByteOps.concat( msg_, sig_ );

        return Base64.encodeToString( raw, 0, raw.length, Base64.DEFAULT );
    }
}
