function viewDefinitionImg(procdefId) {
  var url = ctx + "definition/viewDefinitionImg?procdefId=" + procdefId;
  var options = {
    title: '查看流程图',
    width: window.innerWidth,
    height: window.innerHeight,
    url: url,
    btn: 0,
    shadeClose: true,
    yes: function (index, layero) {
      $.modal.close(index);
    }
  };
  $.modal.openOptions(options);
}

function viewProcessingImg(procInstId) {
  var url = ctx + "process/viewProcessingImg?procInstId=" + procInstId;
  var options = {
    title: '查看流程图',
    width: window.innerWidth,
    height: window.innerHeight,
    url: url,
    btn: 0,
    shadeClose: true,
    yes: function (index, layero) {
      $.modal.close(index);
    }
  };
  $.modal.openOptions(options);
}

/**
 * 格式化时间差，智能显示单位
 * - 小于1分钟：显示 秒
 * - 小于1小时：显示 分 秒  
 * - 小于1天：显示 时 分
 * - 大于等于1天：显示 天 时
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时间字符串
 */
const formatTotalDateSub = (seconds) => {
  // 边界处理
  if (seconds === null || seconds === undefined || isNaN(seconds)) {
    return '-';
  }

  const totalSeconds = Math.abs(Math.floor(seconds));

  if (totalSeconds === 0) {
    return '0 秒';
  }

  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const secs = totalSeconds % 60;

  const parts = [];

  // 有天时：显示 天 时（不显示分秒）
  if (days > 0) {
    parts.push(`${days} 天`);
    if (hours > 0) parts.push(`${hours} 时`);
    return parts.join(' ');
  }

  // 有小时时：显示 时 分（不显示秒）
  if (hours > 0) {
    parts.push(`${hours} 时`);
    if (minutes > 0) parts.push(`${minutes} 分`);
    return parts.join(' ');
  }

  // 有分钟时：显示 分 秒
  if (minutes > 0) {
    parts.push(`${minutes} 分`);
    if (secs > 0) parts.push(`${secs} 秒`);
    return parts.join(' ');
  }

  // 只有秒
  return `${secs} 秒`;
};
