<template>
  <div class="bg">
    <el-container class="wechat">
      <el-aside width="35%" style="border-right: 1px solid #fff">
        <!-- 自己 -->
        <div class="item">
          <el-avatar
            :size="46"
            :src="user.avatarUrl"
            style="float: left; margin-left: 2px"
          ></el-avatar>
          <div class="name">
            {{ user.nickname
            }}<el-tag style="margin-left: 5px" type="success">本人</el-tag>
          </div>
        </div>
        <!-- 在线用户 -->
        <div
          class="item"
          v-for="(item1, index) in userlist"
          :key="item1.uid"
          @click="selectUser(index)"
        >
          <!-- 新数消息 -->
          <el-badge
            :value="new_message_num[index]"
            :max="99"
            :hidden="!new_message_num[index] > 0"
            style="float: left; margin-left: 2px"
          >
            <el-avatar :size="46" :src="item1.avatarUrl"></el-avatar>
          </el-badge>
          <div class="name">{{ item1.nickname }}</div>
        </div>
      </el-aside>
      <el-main>
        <el-container class="wechat_right">
          <!-- 右边顶部 -->
          <el-header class="header">{{
            anotherUser != null && anotherUser.uid > 0
              ? anotherUser.nickname
              : "未选择聊天对象"
          }}</el-header>
          <!-- 聊天内容 -->
          <el-main class="showChat">
            <div v-for="item2 in messageList[index]" :key="item2.msg">
              <!-- 对方发的 -->
              <div class="leftBox" v-if="item2.FromUid == anotherUser.uid">
                <span style="font-size: 4px">{{ item2.time }}</span
                >{{ item2.msg }}
              </div>
              <div class="myBr" v-if="item2.FromUid == anotherUser.uid"></div>
              <!-- 自己发的 -->
              <div class="rightBox" v-if="item2.FromUid == user.uid">
                <span style="font-size: 4px">{{ item2.time }}</span
                >{{ item2.msg }}
              </div>
              <div class="myBr" v-if="item2.FromUid == user.uid"></div>
            </div>
          </el-main>
          <!-- 输入框 -->
          <el-main class="inputValue">
            <textarea v-model="inputValue" id="chat" cols="26" rows="5">
            </textarea>
            <!-- 发送按钮 -->
            <el-button
              v-if="
                anotherUser != null && anotherUser.uid > 0 && inputValue != ''
              "
              type="success"
              size="mini"
              round
              id="send"
              @click="senMessage"
              >发送</el-button
            >
          </el-main>
        </el-container>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      //自己
      user: {},
      //要私信的人
      anotherUser: {},
      //在线的用户
      userlist: [],
      //要私信的人在userlist的索引位置
      index: 0,
      //消息队列集合 [本人和第一个人之间的消息集合、本人和第二个人之间的消息集合、...]
      messageList: [],
      //新消息个数集合
      new_message_num: [],
      //将要发送的内容
      inputValue: "",
      //websocket
      websocket: null,
    };
  },
  methods: {
    //获取自己被分配的信息
    getYourInfo(uid) {
      let params = new URLSearchParams();
      this.$axios
        .post("/user/getYourInfo/" + uid, params)
        .then((res) => {
          this.user = res.data.data;
          if (res.data.code == 200) {
            //获取在线用户
            this.getUserList();
          }
        })
        .catch((err) => {
          console.error(err);
        });
    },
    //获取在线用户
    getUserList() {
      let params = new URLSearchParams();
      this.$axios
        .post("/user/getUserList", params)
        .then((res) => {
          this.userlist = res.data.data.filter(
            //去掉自己
            (user) => user.uid !== this.user.uid
          );
          //填充消息数据 messagelist:[[]、[]...]  并且将新消息队列置为0
          for (let i = 0; i < this.userlist.length; i++) {
            this.messageList.push([]);
            this.new_message_num.push(0);
          }
          //将当前的客户端和服务端进行连接，并定义接收到消息的处理逻辑
          this.init(this.user.uid);
        })
        .catch((err) => {
          console.error(err);
        });
    },
    //选择聊天对象
    selectUser(index) {
      this.anotherUser = this.userlist[index];
      this.index = index;
      //将新消息置为0
      this.new_message_num[index] = 0;
    },
    //将当前的客户端和服务端进行连接，并定义接收到消息的处理逻辑
    init(uid) {
      var self = this;
      if (typeof WebSocket == "undefined") {
        console.log("您的浏览器不支持WebSocket");
        return;
      }
      //清除之前的记录
      if (this.websocket != null) {
        this.websocket.close();
        this.websocket = null;
      }
      //-----------------------连接服务器-----------------------
      let socketUrl = "ws://localhost:8888/wechat";
      //开启WebSocket 连接
      this.websocket = new WebSocket(socketUrl);

      //指定连接成功后的回调函数
      this.websocket.onopen = function () {
        console.log("websocket已打开");
        //发送一次注册消息（使后端先绑定channel和用户的关系，以至于找到对应的channel转发消息）
        let message = {
          FromUid: uid,
          ToUid: uid,
          msg: uid + "的绑定消息",
          time: new Date().toLocaleTimeString(),
        };
        self.websocket.send(JSON.stringify(message));
      };
      //指定连接失败后的回调函数
      this.websocket.onerror = function () {
        console.log("websocket发生了错误");
      };
      //指定当从服务器接受到信息时的回调函数
      this.websocket.onmessage = function (msg) {
        //消息体例如{"FromUid":1,"ToUid":2,"msg":"你好","time":"00:07:03"} => message对象
        let data = JSON.parse(msg.data);
        //添加到对应的消息集合中
        let index = data.FromUid > uid ? data.FromUid - 2 : data.FromUid - 1;
        self.messageList[index].push(data);
        //新消息数+1
        self.new_message_num[index]++;
      };
      //指定连接关闭后的回调函数
      this.websocket.onclose = function () {
        console.log("websocket已关闭");
      };
    },
    //发送信息
    senMessage() {
      //消息体例如{"FromUid":1,"ToUid":2,"msg":"你好","time":"00:07:03"}
      let message = {
        FromUid: this.user.uid,
        ToUid: this.anotherUser.uid,
        msg: this.inputValue,
        time: new Date().toLocaleTimeString(),
      };
      //将消息插进消息队列，显示在前端
      this.messageList[this.index].push(message);
      //将消息发送至服务器端再转发到对应的用户
      this.websocket.send(JSON.stringify(message));
      //清空一下输入框内容
      this.inputValue = "";
    },
  },
  created() {
    let uid = this.$route.query.uid;
    if (uid != undefined) {
      //获取被分配的用户信息
      this.getYourInfo(uid);
    }
  },
};
</script>

<style>
/*改变滚动条 */
::-webkit-scrollbar {
  width: 3px;
  border-radius: 4px;
}

::-webkit-scrollbar-track {
  background-color: inherit;
  -webkit-border-radius: 4px;
  -moz-border-radius: 4px;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background-color: #c3c9cd;
  -webkit-border-radius: 4px;
  -moz-border-radius: 4px;
  border-radius: 4px;
}
.bg {
  background: url("https://s1.ax1x.com/2022/06/12/Xgr9u6.jpg") no-repeat top;
  background-size: cover;
  background-attachment: fixed;
  width: 100%;
  height: 100%;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}
.wechat {
  width: 60%;
  height: 88%;
  margin: 3% auto;
  border-radius: 20px;
  background-color: rgba(245, 237, 237, 0.3);
}
/*聊天框左侧 */
.item {
  position: relative;
  width: 94%;
  height: 50px;
  margin-bottom: 3%;
  border-bottom: 1px solid #fff;
}
.item .name {
  line-height: 50px;
  float: left;
  margin-left: 10px;
}
/*聊天框右侧 */

.wechat_right {
  position: relative;
  width: 100%;
  height: 100%;
}
.header {
  text-align: left;
  height: 50px !important;
}
.showChat {
  width: 100%;
  height: 65%;
}
.inputValue {
  position: relative;
  margin: 0;
  padding: 0;
  width: 100%;
  height: 50%;
}
.inputValue #chat {
  font-size: 18px;
  width: 96%;
  height: 94%;
  border-radius: 20px;
  resize: none;
  background-color: rgba(245, 237, 237, 0.3);
}
#send {
  position: absolute;
  bottom: 12%;
  right: 6%;
}
/*展示区 */
.leftBox {
  float: left;
  max-width: 60%;
  padding: 8px;
  position: relative;
  font-size: 18px;
  border-radius: 12px;
  background-color: rgba(40, 208, 250, 0.76);
}
.rightBox {
  float: right;
  max-width: 60%;
  padding: 8px;
  font-size: 18px;
  border-radius: 12px;
  position: relative;
  background-color: rgba(101, 240, 21, 0.945);
}
.myBr {
  float: left;
  width: 100%;
  height: 20px;
}
.leftBox > span {
  left: 3px;
  width: 120px;
  position: absolute;
  top: -16px;
}
.rightBox > span {
  width: 120px;
  position: absolute;
  right: 3px;
  top: -16px;
}
</style>
