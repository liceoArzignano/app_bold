#include <jni.h>


JNIEXPORT jstring JNICALL
Java_it_liceoarzignano_bold_safe_SafeActivity_getKey(JNIEnv *env, jobject instance) {

    char primKey[69] = {'e', 'e', 'z', 'r', 'W', 'v', 's', 'J',
                         'W', 'm', 'a', 'i', 'c', '7', '/', 'D',
                         'D', '2', 'd', 't', '5', 'g', '=', '=',
                         ':', 'x', 'u', 'f', '8', 'g', 'X', 'H',
                         '8', '7', 'f', 'U', 'z', 'e', 'G', 'B',
                         'I', 'M', 'm', 'n', '+', 'P', 'v', 'e',
                         'C', 'g', 'x', '4', 'g', 'X', 'c', 'l',
                         '6', '1', '0', 'G', 'u', 'Y', 'O', 'M',
                         'F', 'S', 'j', 'o', '='};

    return (*env)->NewStringUTF(env, primKey);
}

JNIEXPORT jstring JNICALL
Java_it_liceoarzignano_bold_safe_SafeActivity_getSalt(JNIEnv *env, jobject instance) {

     char salt[162] = {'3', 'o', 'U', 'j', 'Z', 'k', '/', 'h', 'B',
                       '6', 'b', '9', 'K', '/', '1', 'Z', 'f', '6',
                       'p', 'f', 'g', 'P', 'y', '/', 'w', 'f', 'B',
                       'p', 'S', 'P', 'f', 'f', 'G', '8', 'A', 'X',
                       'w', 'j', 'l', 'q', 'o', 'u', 'W', 'F', 'E',
                       'C', 'x', 'b', 'K', 'j', 'J', 'H', '9', '5',
                       't', 'V', 'g', 'F', 'D', '6', 'Z', 'Y', 'u',
                       'G', '4', 'o', 'd', 'B', 'N', 'j', 'Y', 'C',
                       'h', '+', 'P', 'q', 'u', 'K', 'v', 'K', 'P',
                       'u', 'z', '/', '0', '0', 'K', 'X', 'z', 'q',
                       'A', 'o', 'n', '2', '/', 'f', 'r', 't', 'w',
                       '7', '8', '3', '/', 'N', 'm', 'm', 'b', '1',
                       'w', '7', 'G', 'g', 'W', '0', 'o', '7', '3',
                       'B', 'o', 'J', 't', 'R', 'P', '6', 'p', '3',
                       'g', '9', 'A', 'z', 'D', 'A', 'w', 'M', 'k',
                       'g', 'G', 'Z', 'X', 'U', 'p', 'Y', 'H', 'i',
                       '7', 't', '9', 'f', 'Y', 'C', 'i', 'h', 'x',
                       'h', 'Y', '3', 's', 'i', 'V', 's', 'a', 'y',
                       '+', 'T', 'z', 'o', 's', '0', 'i', '0', 'k',
                       '='};


    return (*env)->NewStringUTF(env, salt);
}