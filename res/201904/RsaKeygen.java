package cn.flyinfree;

import java.math.BigInteger;

@SuppressWarnings("WeakerAccess")
public class RsaKeygen {
    private BigInteger N;
    private BigInteger fy;

    /**
     * 生成密钥对
     * @param e 公钥的e
     * @return [[n,e],[n,d]]
     */
    public BigInteger[][] genKey(BigInteger e){
        BigInteger[] rxy = extGcd(e,fy);
        if(rxy[1].signum()!=1){
            throw new IllegalArgumentException("Can't solve x | 给定条件无法计算逆元");
        }
        BigInteger d = rxy[1];
        return new BigInteger[][]{{N,e},{N,d}};
    }

    /**
     * 求ax+by=1的整数解（用于求逆元）
     * @param a a
     * @param b b
     * @return [r,x,y]
     */
    private BigInteger[] extGcd(BigInteger a, BigInteger b){
        if(b.equals(BigInteger.ZERO)){
            return new BigInteger[]{a,BigInteger.ONE,BigInteger.ZERO};
        }else{
            BigInteger[] tmp = extGcd(b,a.mod(b));
            BigInteger tmpR = tmp[0];
            BigInteger tmpX = tmp[1];
            BigInteger tmpY = tmp[2];

            //noinspection UnnecessaryLocalVariable,SuspiciousNameCombination
            BigInteger x = tmpY;
            BigInteger y = tmpX.subtract(a.divide(b).multiply(tmpY));
            return new BigInteger[]{tmpR,x,y};
        }
    }

    /**
     * fy=(p-1)*(q-1)
     * @param p p
     * @param q q
     */
    public RsaKeygen(BigInteger p, BigInteger q){
        N = p.multiply(q);
        fy = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
    }

    /**
     * default p & q
     * N 340 digits
     */
    @SuppressWarnings("unused")
    public RsaKeygen(){
        this(new BigInteger("6864797660130609714981900799081393217269435300143305409394463459185543183397656052122559640661454554977296311391480858037121987999716643812574028291115057151"),
                new BigInteger("531137992816767098689588206552468627329593117727031923199444138200403559860852242739162502265229285668889329486246501015346579337652707239409519978766587351943831270835393219031728127"));
    }
}
