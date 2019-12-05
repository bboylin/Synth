### Synth : 利用ASM在编译器内联access方法

----


写Android的肯定注意到过：断点调试有时候stacktrace里会出现自己代码里没有出现的方法，而且命名是access$加上一串数字。其实，access方法是Java语法糖生成的synthetic方法，目的是为了实现内部类和外部类之间private member和private field的访问，更具体地举例说，之所以外部类能访问内部类的私有变量，是因为Java生成了个package访问级别的access方法，同时修改了指令，把所有对内部类私有变量的访问变成了对access方法的调用，读写都是如此。反之亦然，内部类访问外部类的私有成员变量/私有static变量/私有方法/私有static方法都是通过access方法实现的。

如果还不理解的可以看看jake wharton的这个presentation：[Exploring Java Hidden Costs](https://jakewharton.com/exploring-java-hidden-costs/)

synth通过ASM在编译期间移除了access方法， 同时将被外部类或者内部类访问的私有变量/私有static变量/私有方法/私有static方法都变成package级别的访问权限，将所有对access方法的调用转为直接对变量或者方法的调用。

简而言之 就是内联了access方法的调用。这样做有什么好处呢？
* 减少了方法数，有利于缓解安卓上存在的65535方法数限制，同时能减少包体积
* 少了一层方法调用，指令更紧凑，执行效率更高。（当然，现代机器上这点已经可以忽略了）

Facebook开源的Android编译工具 [redex](https://github.com/facebook/redex) 中已经做了一样的事情，不同的是其实现是基于对dex字节码的操作：[https://github.com/facebook/redex/blob/26c54aa0e9e8ac063d721f7b70ab11fd2298d6a9/opt/synth/Synth.cpp](https://github.com/facebook/redex/blob/26c54aa0e9e8ac063d721f7b70ab11fd2298d6a9/opt/synth/Synth.cpp), 而且gradle项目必须改用redex编译才能使用这个feature。那么在不从gradle迁移到redex的前提下，这个feature是无法使用的。

字节跳动西瓜技术团队也做了这样的事情，利用ASM在编译器内联access方法，作为一个独立的gradle插件。详见：[西瓜视频apk瘦身之 Java access 方法删除](https://mp.weixin.qq.com/s/ZHisCVjO_ZrtvvEWBYUQFQ)

不过他们没有开源。于是我按照头条的思路实现了一下，代码开源在这个仓库。

实现细节：
* 为什么使用ASM的Tree API而不是很多人用的core API？Tree API官方文档说耗时大概是core API的1.3倍。但是考虑到这种业务场景下core API需要两次 class parse的遍历，一次收集access方法和类/字段信息，另一次执行bytecode manipulation，而Tree API只需要一次，显然更快而且更优雅。
* 是不是简单的把access方法里的指令照搬到调用处替换下就能实现内联？大体可以这么考虑 但是细节问题还是不少，不如同样的aload_0指令 在两个方法里的含义是不一样的，因为不同类里局部变量表里第一个成员不同。另外方法的access从private变成package级别后，调用指令也变了，原先invokespecial调用这个方法的地方都得改成invokevirtual，还有指令增加带来的操作数栈的扩展等等。
* 如何尽可能控制这个工具自身的风险？ASM提供了`CheckClassAdapter`用于对字节码编译的校验，尽可能将问题暴露在编译期，从而不带入线上。如果遇到编译出错，可以将具体报错贴出来issue我。同时如果想引用这个工具，最好充分测试app功能的正确性再上线。


#### 如何引用：

##### 1. 在project的build.gradle添加

```groovy
buildscript {
    dependencies {
        classpath 'xyz.bboylin:synth:0.0.1'
    }
}
```
##### 2. 在app的build.gradle中添加

```groovy
apply plugin: 'xyz.bboylin.synth'
```

---

any questions contact me : bboylin24@gmail.com

welcome for issues and pull requests

## License

    Copyright 2019 bboylin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

