package com.unipi.msc.javablockchainapi.interfaces;

public interface NonceListener {
    void OnNonceFound(int nonce, String hash);
}
