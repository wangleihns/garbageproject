package com.jin.env.garbage.utils;

/**
 * Created by abc on 2018/5/24.
 */
public class Constants {
    public enum loginStatus{
        UsernameNotFound(10010),
        BadCredentials(10011),
        AccountExpired(10012),
        Locked(10013),
        Disabled(10014),
        CredentialsExpired(10015),
        LoginSuccess(200);
        private Integer status;

        loginStatus(Integer status) {
            this.status = status;
        }
        public Integer getStatus() {
            return status;
        }
    }

    public enum loginType{
        GarbageCar("garbage_car"),
        NoGarbageCar("no_garbage_car"),
        Android("android"),
        IPhone("iphone"),
        Web("web"),
        Pad("pad"),
        IPad("ipad");
        private String type;
        loginType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }


    public enum accountStatus{
        UsernameNotFound("用户找不到"),
        BadCredentials("密码错误"),
        AccountExpired("账户过期"),
        Locked("账户锁定"),
        Disabled("账户不可用"),
        CredentialsExpired("证书过期");
        private String status;

        accountStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    public enum tokenStatus {
        TokenExp(2000),
        TokenChange(3000),
        TOKEN_NOT_EXIST(5000),
        SignNotRight(4000);
        private Integer status;
        tokenStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    /**
     * 签名秘钥
     */
    public enum signSecret {
        Secret("www.winding.top");
        private String content;

        signSecret(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public enum responseStatus{
        Success(200),
        Failure(500);
        private int status;

        responseStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    /**
     * 数据状态
     */
    public enum dataType{
        //不可用
        DISABLE(0),
        //可用
        ENABLE(1);
        private int type;

        dataType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    /**
     * 图标类型枚举
     */
    public enum image{
        //头像
        HEADER,
        //头像略缩图
        HEADER_SMALL,
        /**
         * 二维码
         */
        QRCODE,
        /**
         * 垃圾图片
         */
        GARBAGE_IMAGE,
        /**
         * 美丽庭院
         */
        FOLD
        ;



        private String type;
    }

    /**
     * 垃圾类型
     */
    public enum garbageType{
        /**
         * 厨余垃圾
         */
        KITCHEN_GARBAGE(1),
        /**
         * 其他垃圾
         */
        OTHER_GARBAGE(2),
        /**
         * 可回收垃圾
         */
        RECYCLEABLE(3),
        /**
         * 有害垃圾
         */
        DANGER_GARBAGE(4),
        /**
         *  可回收- 铝金属
         */
        AL(5),
        /**
         * 可回收 - 铁
         */
        FE(6),
        /**
         * 可回收 - 铜
         */
        TU(7),
        /**
         * 可回收 - 纸张
         */
        PAPER(8),
        /**
         * 可回收 - 瓶子
         */
        BOTTEL(9),
        /**
         * 可回收 - 其他
         */
        RECYCLEOHTER(10);


        private Integer type;

        garbageType(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return type;
        }
    }

    /**
     * 垃圾质量
     */
    public enum garbageQuality{
        /**
         * 合格
         */
        QUALIFIED(1),
        /**
         * 不合格
         */
        NOTQUALIFIED(2),
        /**
         * 空桶
         */
        EMPTY(3);
        private Integer type;

        garbageQuality(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return type;
        }
    }

    /**
     * 垃圾来源客户端
     */
    public enum garbageFromClient{
        /**
         * 人工环卫车
         */
        GARBAGETRUCK(0),
        /**
         * 自动环卫车
         */
        AUTOTRUCK(1);
        private Integer type;

        garbageFromClient(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return type;
        }
    }

    /**
     * 根据地区设定垃圾质量分类积分
     */
    public enum garbageFromType {
        /**
         * 农村
         */
        TOWN(0),
        /**
         * 小区
         */
        COMMUNITY(1);
        private Integer type;

        garbageFromType(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return type;
        }
    }

    public enum taskStatus {
        WAITING,
        RUNNING,
        NO_ACTIVE
    }

    public enum taskType{
        START,  //开始定时任务
        END     // 结束定时任务
    }

    public enum sMsModelResult{
        QUALITY("合格"),
        NOTQUALITY("不合格"),
        EMPTY("显示为空桶");

        private String content;

        sMsModelResult(String content) {
            this.content = content;
        }
        public String getContent() {
            return content;
        }
    }

    public enum sMsModelRemark{
        QUALITY("请继续保持"),
        NOTQUALITY("请您重视垃圾分类"),
        EMPTY("请您积极参与垃圾分类");

        private String content;

        sMsModelRemark(String content) {
            this.content = content;
        }
        public String getContent() {
            return content;
        }
    }

    public enum messageStatus{
        SEND(1),
        NOTSEND(0);

        messageStatus(Integer code) {
            this.code = code;
        }

        private Integer code;

        public Integer getCode() {
            return code;
        }
    }

    /**
     * 垃圾分类卡的类型
     */
    public enum eNoType{
        /**
         * 厨余垃圾
         */
        KITCHEN_CARD(1),
        /**
         * 其他垃圾
         */
        OTHER_CARD(2),
        /**
         * 可回收垃圾
         */
        RECYCLEABLE_CARD(3),
        /**
         * 有害垃圾
         */
        DANGER_CARD(4),;

        private int type;

        eNoType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

}
