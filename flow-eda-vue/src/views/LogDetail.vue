<template>
  <div id="log-detail" class="log-detail">
    <Codemirror
      v-model:value="logContent"
      :options="{
        mode: 'javascript',
        styleActiveLine: true,
        theme: 'dracula',
        readOnly: true,
      }"
      style="font-size: 14px"
    />
  </div>
</template>

<script>
import Codemirror from "codemirror-editor-vue3";
import "codemirror/mode/javascript/javascript.js";
import "codemirror/theme/dracula.css";
import { onBeforeUnmount, ref, watch } from "vue";
import { onCloseLogDetail, onOpenLogDetail } from "../api/ws.js";

export default {
  name: "LogDetail",
  props: {
    path: String,
  },
  components: {
    Codemirror,
  },
  setup(props) {
    const logContent = ref("");

    // 监听参数变化，加载新数据，关闭旧连接
    watch(
      () => props.path,
      (n, o) => {
        if (n) {
          if (n !== o) {
            onCloseLogDetail(o);
            getData(n);
          }
        }
      }
    );

    // 获取日志内容
    const getData = (path) => {
      logContent.value = "";
      onOpenLogDetail(path, (s) => {
        logContent.value = logContent.value.concat(s);
      });
    };

    // 初始加载
    getData(props.path);

    // 组件被销毁之前，关闭socket连接
    onBeforeUnmount(() => {
      onCloseLogDetail(props.path);
    });

    return {
      logContent,
    };
  },
};
</script>

<style>
.log-detail {
  width: 100%;
  height: 100%;
}
</style>
