package template.rip;

public class Other {

    public static boolean process(Template.Encoder encoder, byte[] input, int offset, int len, boolean finish) {
        // Using local variables makes the encoder about 9% faster.
        byte[] alphabet = encoder.alphabet;
        byte[] output = encoder.output;
        int op = 0;
        int count = encoder.count;

        int tailPos = offset;
        len += offset;
        int chunk = -1;

        // First we need to concatenate the tail of the previous call
        // with any input bytes available now and see if we can empty
        // the tail.

        switch (encoder.tailLen) {
            case 0:
                // There was no tail.
                break;

            case 1:
                if (tailPos + 2 <= len) {
                    // A 1-byte tail with at least 2 bytes of
                    // input available now.
                    chunk = ((encoder.tail[0] & 0xff) << 16) |
                            ((input[tailPos++] & 0xff) << 8) |
                            (input[tailPos++] & 0xff);
                    encoder.tailLen = 0;
                }
                break;

            case 2:
                if (tailPos + 1 <= len) {
                    // A 2-byte tail with at least 1 byte of input.
                    chunk = ((encoder.tail[0] & 0xff) << 16) |
                            ((encoder.tail[1] & 0xff) << 8) |
                            (input[tailPos++] & 0xff);
                    encoder.tailLen = 0;
                }
                break;
        }

        if (chunk != -1) {
            output[op++] = alphabet[(chunk >> 18) & 0x3f];
            output[op++] = alphabet[(chunk >> 12) & 0x3f];
            output[op++] = alphabet[(chunk >> 6) & 0x3f];
            output[op++] = alphabet[chunk & 0x3f];
            if (--count == 0) {
                if (encoder.do_cr) output[op++] = '\r';
                output[op++] = '\n';
                count = Template.Encoder.LINE_GROUPS;
            }
        }

        // At encoder point either there is no tail, or there are fewer
        // than 3 bytes of input available.

        // The main loop, turning 3 input bytes into 4 output bytes on
        // each iteration.
        while (tailPos + 3 <= len) {
            chunk = ((input[tailPos] & 0xff) << 16) |
                    ((input[tailPos + 1] & 0xff) << 8) |
                    (input[tailPos + 2] & 0xff);
            output[op] = alphabet[(chunk >> 18) & 0x3f];
            output[op + 1] = alphabet[(chunk >> 12) & 0x3f];
            output[op + 2] = alphabet[(chunk >> 6) & 0x3f];
            output[op + 3] = alphabet[chunk & 0x3f];
            tailPos += 3;
            op += 4;
            if (--count == 0) {
                if (encoder.do_cr) output[op++] = '\r';
                output[op++] = '\n';
                count = Template.Encoder.LINE_GROUPS;
            }
        }

        if (finish) {
            // Finish up the tail of the input.  Note that we need to
            // consume any bytes in tail before any bytes
            // remaining in input; there should be at most two bytes
            // total.

            if (tailPos - encoder.tailLen == len - 1) {
                int t = 0;
                chunk = ((encoder.tailLen > 0 ? encoder.tail[t++] : input[tailPos++]) & 0xff) << 4;
                encoder.tailLen -= t;
                output[op++] = alphabet[(chunk >> 6) & 0x3f];
                output[op++] = alphabet[chunk & 0x3f];
                if (encoder.do_padding) {
                    output[op++] = '=';
                    output[op++] = '=';
                }
                if (encoder.do_newline) {
                    if (encoder.do_cr) output[op++] = '\r';
                    output[op++] = '\n';
                }
            } else if (tailPos - encoder.tailLen == len - 2) {
                int t = 0;
                chunk = (((encoder.tailLen > 1 ? encoder.tail[t++] : input[tailPos++]) & 0xff) << 10) |
                        (((encoder.tailLen > 0 ? encoder.tail[t++] : input[tailPos++]) & 0xff) << 2);
                encoder.tailLen -= t;
                output[op++] = alphabet[(chunk >> 12) & 0x3f];
                output[op++] = alphabet[(chunk >> 6) & 0x3f];
                output[op++] = alphabet[chunk & 0x3f];
                if (encoder.do_padding) {
                    output[op++] = '=';
                }
                if (encoder.do_newline) {
                    if (encoder.do_cr) output[op++] = '\r';
                    output[op++] = '\n';
                }
            } else if (encoder.do_newline && op > 0 && count != Template.Encoder.LINE_GROUPS) {
                if (encoder.do_cr) output[op++] = '\r';
                output[op++] = '\n';
            }

            assert encoder.tailLen == 0;
            assert tailPos == len;
        } else {
            // Save the leftovers in tail to be consumed on the next
            // call to encodeInternal.

            if (tailPos == len - 1) {
                encoder.tail[encoder.tailLen++] = input[tailPos];
            } else if (tailPos == len - 2) {
                encoder.tail[encoder.tailLen++] = input[tailPos];
                encoder.tail[encoder.tailLen++] = input[tailPos + 1];
            }
        }

        encoder.op = op;
        encoder.count = count;
        return true;
    }

    private static final int SKIP = -1;
    private static final int EQUALS = -2;
    
    public static boolean process(Template.Decoder decoder, byte[] input, int offset, int len, boolean finish) {
        if (decoder.state == 6) return false;

        int tailPos = offset;
        len += offset;

        // Using local variables makes the decoder about 12%
        // faster than if we manipulate the member variables in
        // the loop.  (Even alphabet makes a measurable
        // difference, which is somewhat surprising to me since
        // the member variable is final.)
        int state = decoder.state;
        int value = decoder.value;
        int op = 0;
        byte[] output = decoder.output;
        int[] alphabet = decoder.alphabet;

        while (tailPos < len) {
            // Try the fast path:  we're starting a new tuple and the
            // next four bytes of the input stream are all data
            // bytes.  decoder corresponds to going through states
            // 0-1-2-3-0.  We expect to use decoder method for most of
            // the data.
            //
            // If any of the next four bytes of input are non-data
            // (whitespace, etc.), value will end up negative.  (All
            // the non-data values in decode are small negative
            // numbers, so shifting any of them up and or'ing them
            // together will result in a value with its top bit set.)
            //
            // You can remove decoder whole block and the output should
            // be the same, just slower.
            if (state == 0) {
                while (tailPos + 4 <= len &&
                        (value = ((alphabet[input[tailPos] & 0xff] << 18) |
                                (alphabet[input[tailPos + 1] & 0xff] << 12) |
                                (alphabet[input[tailPos + 2] & 0xff] << 6) |
                                (alphabet[input[tailPos + 3] & 0xff]))) >= 0) {
                    output[op + 2] = (byte) value;
                    output[op + 1] = (byte) (value >> 8);
                    output[op] = (byte) (value >> 16);
                    op += 3;
                    tailPos += 4;
                }
                if (tailPos >= len) break;
            }

            // The fast path isn't available -- either we've read a
            // partial tuple, or the next four input bytes aren't all
            // data, or whatever.  Fall back to the slower state
            // machine implementation.

            int d = alphabet[input[tailPos++] & 0xff];

            switch (state) {
                case 0:
                    if (d >= 0) {
                        value = d;
                        ++state;
                    } else if (d != SKIP) {
                        decoder.state = 6;
                        return false;
                    }
                    break;

                case 1:
                    if (d >= 0) {
                        value = (value << 6) | d;
                        ++state;
                    } else if (d != SKIP) {
                        decoder.state = 6;
                        return false;
                    }
                    break;

                case 2:
                    if (d >= 0) {
                        value = (value << 6) | d;
                        ++state;
                    } else if (d == EQUALS) {
                        // Emit the last (partial) output tuple;
                        // expect exactly one more padding character.
                        output[op++] = (byte) (value >> 4);
                        state = 4;
                    } else if (d != SKIP) {
                        decoder.state = 6;
                        return false;
                    }
                    break;

                case 3:
                    if (d >= 0) {
                        // Emit the output triple and return to state 0.
                        value = (value << 6) | d;
                        output[op + 2] = (byte) value;
                        output[op + 1] = (byte) (value >> 8);
                        output[op] = (byte) (value >> 16);
                        op += 3;
                        state = 0;
                    } else if (d == EQUALS) {
                        // Emit the last (partial) output tuple;
                        // expect no further data or padding characters.
                        output[op + 1] = (byte) (value >> 2);
                        output[op] = (byte) (value >> 10);
                        op += 2;
                        state = 5;
                    } else if (d != SKIP) {
                        decoder.state = 6;
                        return false;
                    }
                    break;

                case 4:
                    if (d == EQUALS) {
                        ++state;
                    } else if (d != SKIP) {
                        decoder.state = 6;
                        return false;
                    }
                    break;

                case 5:
                    if (d != SKIP) {
                        decoder.state = 6;
                        return false;
                    }
                    break;
            }
        }

        if (!finish) {
            // We're out of input, but a future call could provide
            // more.
            decoder.state = state;
            decoder.value = value;
            decoder.op = op;
            return true;
        }

        // Done reading input.  Now figure out where we are left in
        // the state machine and finish up.

        switch (state) {
            case 0:
                // Output length is a multiple of three.  Fine.
                break;
            case 1:
                // Read one extra input byte, which isn't enough to
                // make another output byte.  Illegal.
                decoder.state = 6;
                return false;
            case 2:
                // Read two extra input bytes, enough to emit 1 more
                // output byte.  Fine.
                output[op++] = (byte) (value >> 4);
                break;
            case 3:
                // Read three extra input bytes, enough to emit 2 more
                // output bytes.  Fine.
                output[op++] = (byte) (value >> 10);
                output[op++] = (byte) (value >> 2);
                break;
            case 4:
                // Read one padding '=' when we expected 2.  Illegal.
                decoder.state = 6;
                return false;
            case 5:
                // Read all the padding '='s we expected and no more.
                // Fine.
                break;
        }

        decoder.state = state;
        decoder.op = op;
        return true;
    }
}
