#include <jni.h>
#include <stdio.h>

// Java에서
//
//
//
// 4. 클래스 내에서 활용하려는 메서드를 적어준다(언더바 필요)
JNIEXPORT jstring JNICAL
Java_com_example_demo_nativeinterface_test_HelloSpring_print(JNIEnv *env, jobject obj)
//Java_com_example_demo_HelloSpring_print(JNIEnv *env, jobject obj)
{
    // C 코드의 배열에 문자열을 설정
    char msg[60] = "Hello C Language with Java!";
    // Java와 연동하기 위해 jstring을 사용함(Java와 C가 함께 사용할 수 있는 문자열)
    jstring result;

    // C는 기본적으로 ASCII 코드를 베이스로 동작하는데
    // Java는 UTF(유니코드)를 베이스로 동작하므로 아래와 같은 방식으로 문자열을 설정해야 한다.
    // 자바 관점에서는 "Hello C Language ~~"를 new 했다고 생각
    // 네이버 등 연구소에 들어가지 않는다면 할일은 없음(교양 수업이라 보면됨)
    result = (*env)->NewStringUTF(env, msg);
    return result;
}