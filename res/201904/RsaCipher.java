package cn.flyinfree;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class RsaCipher {
    /**
     * 加密一个文件
     * @param in 输入文件
     * @param out 输出文件
     * @param packSize 分包大小 ( Base64 编码处理后不能超过密钥 N 长度 )
     * @param pub 公钥数组
     * @throws IOException 当文件读写错误时
     */
    public void encryptFile(File in, File out, int packSize, BigInteger[] pub) throws IOException {
        FileInputStream fis = new FileInputStream(in);
        PrintWriter fileWriter = new PrintWriter(new FileWriter(out));
        Base64.Encoder enc = Base64.getEncoder();

        byte[] tmp = new byte[packSize];
        int count;
        do {
            count = fis.read(tmp);
            BigInteger bi;
            if(count != packSize){
                byte[] shortTmp = new byte[count];
                System.arraycopy(tmp,0,shortTmp,0,count);
                bi = encrypt(toBigInteger(shortTmp),pub);
            }else{
                bi = encrypt(toBigInteger(tmp),pub);
            }
            fileWriter.println(enc.encodeToString(bi.toString().getBytes(StandardCharsets.US_ASCII)));
        }while(count == packSize);

        fileWriter.flush();
        fis.close();
        fileWriter.close();
    }
    public void decryptFile(File in, File out, BigInteger[] self) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(in));
        FileOutputStream fos = new FileOutputStream(out);
        Base64.Decoder dec = Base64.getDecoder();

        while(fileReader.ready()){
            byte[] lineBytes = dec.decode(fileReader.readLine());
            byte[] msg = toBytes(decrypt(new BigInteger(new String(lineBytes,StandardCharsets.US_ASCII)),self));
            fos.write(msg);
        }

        fos.flush();
        fileReader.close();
        fos.close();

    }
    public BigInteger encrypt(BigInteger msg, BigInteger[] pub){
        BigInteger N = pub[0];
        BigInteger e = pub[1];

        return expMod(msg,e,N);
    }
    public BigInteger decrypt(BigInteger cipher, BigInteger[] self){
        BigInteger N = self[0];
        BigInteger d = self[1];

        return expMod(cipher,d,N);
    }
    /**
     * 蒙哥马利算法计算大数的大数幂对大数取模 (base^exp) mod n
     * @param base base
     * @param exp　exp
     * @param nMod n
     * @return answer
     */
    private BigInteger expMod(BigInteger base, BigInteger exp, BigInteger nMod){
        char[] binaryArray = new StringBuilder(exp.toString(2))
                .reverse().toString().toCharArray();
        int len = binaryArray.length;

        List<BigInteger> baseArray = new ArrayList<>();
        BigInteger preBase = base;
        baseArray.add(preBase);

        for (int i = 0; i < len - 1; i++) {
            BigInteger nextBase = preBase.multiply(preBase).mod(nMod);
            baseArray.add(nextBase);
            preBase = nextBase;
        }

        BigInteger a_w_b = arrayMultiply(baseArray.toArray(new BigInteger[0]),binaryArray,nMod);

        return a_w_b.mod(nMod);
    }
    private BigInteger arrayMultiply(BigInteger[] array, char[] binaryArray, BigInteger nMod){
        BigInteger result = BigInteger.ONE;

        for (int i = 0; i < array.length; i++) {
            BigInteger a = array[i];
            if(binaryArray[i]=='0'){
                continue;
            }
            result = result.multiply(a);
            result = result.mod(nMod);
        }

        return result;
    }
    private BigInteger toBigInteger(byte[] bytes){
        Base64.Encoder enc = Base64.getEncoder();
        String res = enc.encodeToString(bytes);

        char[] ca = res.toCharArray();

        StringBuilder sb = new StringBuilder("1");
        DecimalFormat df = new DecimalFormat("00");

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < ca.length; i++) {
            int x =ca[i]-42;
            sb.append(df.format(x));
        }

        return new BigInteger(sb.toString());
    }
    private byte[] toBytes(BigInteger c){
        Base64.Decoder dec = Base64.getDecoder();
        String num = c.toString().substring(1);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < num.length() - 1; i+=2) {
            int x =Integer.valueOf(num.substring(i,i+2))+42;
            sb.append((char)x);
        }
        return dec.decode(sb.toString());
    }
}
