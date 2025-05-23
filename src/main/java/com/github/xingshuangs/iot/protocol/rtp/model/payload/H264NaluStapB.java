/*
 * MIT License
 *
 * Copyright (c) 2021-2099 Oscura (xingshuang) <xingshuang_cool@163.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.xingshuangs.iot.protocol.rtp.model.payload;


import com.github.xingshuangs.iot.common.buff.ByteReadBuff;
import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;

import java.util.ArrayList;
import java.util.List;

/**
 * STAP_A
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          RTP Header                           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |STAP-B NAL HDR | DON                           | NALU 1 Size   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | NALU 1 Size   | NALU 1 HDR    | NALU 1 Data                   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               +
 * :                                                               :
 * +               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |               | NALU 2 Size                   | NALU 2 HDR    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                       NALU 2 Data                             |
 * :                                                               :
 * |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                               :...OPTIONAL RTP padding        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * @author xingshuang
 */
public class H264NaluStapB extends H264NaluBase {

    /**
     * Decoding order number.
     * (解码顺序编号)
     */
    private int decodingOrderNumber;

    private final List<H264NaluStapSingle> naluSingles = new ArrayList<>();

    @Override
    public int byteArrayLength() {
        int sum = this.header.byteArrayLength();
        sum += 2;
        for (H264NaluStapSingle item : this.naluSingles) {
            sum += item.byteArrayLength();
        }
        return sum;
    }

    @Override
    public byte[] toByteArray() {
        ByteWriteBuff buff = ByteWriteBuff.newInstance(this.byteArrayLength());
        buff.putBytes(this.header.toByteArray());
        buff.putShort(this.decodingOrderNumber);
        for (H264NaluStapSingle item : this.naluSingles) {
            buff.putBytes(item.toByteArray());
        }
        return buff.getData();
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data byte array
     * @return RtcpHeader
     */
    public static H264NaluStapB fromBytes(final byte[] data) {
        return fromBytes(data, 0);
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data   byte array
     * @param offset index offset
     * @return RtcpHeader
     */
    public static H264NaluStapB fromBytes(final byte[] data, final int offset) {
        if (data.length < 1) {
            throw new IndexOutOfBoundsException("H264NaluStapSingle, data length < 1");
        }
        int index = offset;
        H264NaluStapB res = new H264NaluStapB();

        res.header = H264NaluHeader.fromBytes(data, index);
        index += res.header.byteArrayLength();

        res.decodingOrderNumber = ByteReadBuff.newInstance(data, index).getUInt16();
        index += 2;

        while (index < data.length) {
            H264NaluStapSingle tmp = H264NaluStapSingle.fromBytes(data, index);
            res.naluSingles.add(tmp);
            index += tmp.byteArrayLength();
        }
        return res;
    }
}
