# 日报发送工作流

## Description
本工作流用于自动生成日报并发送到企业微信智能表格。

## Tools
可用工具：
- read_file: 读取文件内容
- write_file: 写入文件内容
- send_http_request: 发送HTTP请求（GET/POST/PUT/DELETE等）
- list_files: 列出目录文件
- write_daily_report: 写日报
- read_daily_report: 读日报
- list_daily_reports: 列出所有日报

## 工作流程

### 步骤1：检查日报文件是否存在
首先检查今天的日报文件是否已存在。日报文件存放在 ./memory/daily_logs/ 目录下，文件名为 yyyy-MM-dd.md 格式。

### 步骤2：生成或加载日报
- 如果日报文件不存在：
  1. 查看今日的对话记录和操作日志
  2. 基于今日活动生成日报内容，包括：
     - 今日工作总结
     - 明日工作计划
     - 其他事项
  3. 将日报保存到 ./memory/daily_logs/yyyy-MM-dd.md

- 如果日报文件已存在：
  1. 读取日报文件内容
  2. 确认内容是否完整，必要时进行补充

### 步骤3：准备发送数据
将日报内容整理成企业微信智能表格所需的格式：
```json
{
  "schema": {
    "fiZ2CL": "填写人",
    "fl8KX3": "所在部门",
    "fcSVPo": "填写时间",
    "fmv3Ai": "汇报日期",
    "fzbz6E": "今日工作总结",
    "fCZIsT": "明日工作计划",
    "fERpbo": "其他事项"
  },
  "add_records": [
    {
      "values": {
        "fiZ2CL": [
          {
            "user_id": ""
          }
        ],
        "fl8KX3": [
          {
            "text": "研发部"
          }
        ],
        "fcSVPo": "当前时间戳(毫秒)",
        "fmv3Ai": "今日日期时间戳(毫秒)",
        "fzbz6E": "今日工作总结内容",
        "fCZIsT": "明日工作计划内容",
        "fERpbo": "其他事项内容"
      }
    }
  ]
}
```

### 步骤4：发送日报
使用 send_http_request 工具将日报发送到以下地址：
https://qyapi.weixin.qq.com/cgi-bin/wedoc/smartsheet/webhook?key=3QLpU6yNldZl5vTd0xoMORiSlkLh2ZDeVabnGn45kzhwP8XUV4z8wgoECUOGDHRBy08WI0vd6deFSSNUIcWhG8ObNsfvKOEbt3LH65IQ8erW

请求方法：POST
请求头：Content-Type: application/json

### 步骤5：反馈结果
向用户报告日报发送结果，包括：
- 发送是否成功
- 日报内容摘要
- 如失败，提供错误信息

## 注意事项
1. 确保日报内容真实反映今日工作
2. 时间戳使用毫秒级Unix时间戳
3. 发送前确认日报内容完整
4. 如发送失败，保存日报以便后续重试
