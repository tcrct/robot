/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.common.telegrams;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;

/**
 * 用于与车辆通信的所有电报类型的基类
 *
 * @author Laotang
 */
public abstract class Telegram implements Serializable {

  /**
   * 默认的报文ID
   */
  public static final String ID_DEFAULT = "0";
  /**
   * 报文协议内容
   */
  protected String rawContent;
  /**
   * 唯一的报文ID
   */
  protected String id;

  /**
   * 构造函数
   *
   * @param telegramLength 报文长度
   */
  public Telegram(String telegramData) {
    this.rawContent = telegramData;
  }

  /**
   * 返回报文内容
   *
   * @return byte[]格式的报文内容
   */
  public String getRawContent() {
    return rawContent;
  }

  /**
   * 取报文内容Byte数组
   * @return 字符串
   */
  public byte[] getRawContentByte() {
    return StrUtil.isBlank(rawContent) ? null : rawContent.getBytes();
  }

  /**
   *  报文ID，唯一
   *
   * @return Mongodb ObjectId格式的字符串
   */
  public String getId() {
    return id;
  }

  // tag::documentation_checksumComp[]
  /**
   * Computes a checksum for the given raw content of a telegram.
   *
   * @param rawContent A telegram's raw content.
   * @return The checksum computed for the given raw content.
   */
  public static byte getCheckSum(byte[] rawContent) {
    requireNonNull(rawContent, "rawContent");

    int cs = 0;
    for (int i = 0; i < rawContent[1]; i++) {
      cs ^= rawContent[2 + i];
    }
    return (byte) cs;
  }
  // end::documentation_checksumComp[]
}
