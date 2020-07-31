package dev.stickbit.speed;

import android.app.Activity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class StrVerify {
    public static String checkString(String res, Activity c, String path, String mode) {
        try {
            //  System.out.println(res);
            String reply = res.substring(0, res.lastIndexOf('\n'));
            reply = reply.replaceAll("\r", "");
            //  System.out.println("Reply is: " + reply);
            String hash = res.substring(res.lastIndexOf('\n') + 1);
            // System.out.println("Hash is: " + hash);
            String decompReply = new String(Base64.getDecoder().decode(hash), StandardCharsets.UTF_8).replaceAll("\n", "").trim();
            decompReply = decompReply.replaceAll("\n", "");
            if (!decompReply.equals(reply.replaceAll("\n", "").trim())) {
                System.out.println("We got: " + decompReply);
                System.out.println("They got: " + reply);
                throw new Exception("bad lol");
            }
            return reply.trim();
        } catch (Exception e) {
            return null;
        }
    }
}
