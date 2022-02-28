package com.agorachatapp.tokener.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
