/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.sidelab.scstack.lib.commons;


/**
 *
 * @author root
 */
public class Md5 {

    static char[] carr = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    
    public static String getBase64FromHEX(String input) {
        byte barr[] = new byte[16];
        int bcnt = 0;
        for (int i = 0; i < 32; i += 2) {
            char c1 = input.charAt(i);
            char c2 = input.charAt(i + 1);
            int i1 = intFromChar(c1);
            int i2 = intFromChar(c2);

            barr[bcnt] = 0;
            barr[bcnt] |= (byte) ((i1 & 0x0F) << 4);
            barr[bcnt] |= (byte) (i2 & 0x0F);
            bcnt++;
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return "{MD5}" + encoder.encode(barr);
    }




    private static int intFromChar(char c) {
        char clower = Character.toLowerCase(c);
        for (int i = 0; i < carr.length; i++) {
            if (clower == carr[i]) {
                return i;
            }
        }

        return 0;
    }

}