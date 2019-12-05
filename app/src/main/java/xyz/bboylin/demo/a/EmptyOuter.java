package xyz.bboylin.demo.a;

/**
 * Created by bboylin on 2019-10-30.
 */
public class EmptyOuter {
    void test() {
        EmptyInner inner = new EmptyInner();
        inner.mLong = 2;
        inner.log();
    }

    private class EmptyInner {
        long mLong = 1;

        void log() {
        }

        void callPackageMethod() {
            log();
        }
    }

    private static class StaticInner {
        private static int sInt;
    }
}
