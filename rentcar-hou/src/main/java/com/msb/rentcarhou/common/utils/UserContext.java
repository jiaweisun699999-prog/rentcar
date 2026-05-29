package com.msb.rentcarhou.common.utils;

public class UserContext {
    /**
     * ThreadLocal 是 Java
     * 提供的一个特殊的类。你可以把它想象成一个带锁的储物柜，每个来访问它的“线程”（Thread）都会自动获得一个专属的、别人看不到的抽屉。
     * 在 Spring Boot（或 Tomcat）这样的 Web 服务器中，每一个前端发来的 HTTP 请求，服务器通常都会分配一个独立的线程来处理。
     * 因为每个线程都有自己专属的“抽屉”，所以当用户 A 和用户 B 同时发请求时，哪怕他们同时执行到这行代码，A 的用户 ID 也只会存放在 A
     * 线程的抽屉里，B 的 ID 只会存在 B 线程的抽屉里，绝对不会发生数据串用和互相干扰。
     */
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 存入用户 ID
     * 这个方法通常会在请求刚进入服务器时被调用（比如在拦截器 Interceptor 或过滤器 Filter 中）。
     * 当系统解析完前端传来的 Token，确认了当前是谁在操作后，就会调用这个方法，把 userId 存入当前处理该请求的线程的 ThreadLocal
     * “抽屉”里。
     * 
     * @param userId
     */
    public static void setUserId(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取用户 ID
     * 这就是您在之前 CarOrderServiceImpl 里面看到的 Long currentUserId =
     * com.msb.rentcarhou.common.utils.UserContext.getUserId(); 的来源。
     * 因为在第 2 步已经把 ID 存进去了，所以只要请求还没结束（还在同一个线程里运行），不管代码走到了 Controller 还是 Service 还是
     * Mapper 层，只需调用这个静态方法，就能随时从当前线程的“抽屉”里把用户 ID 拿出来用，省去了在方法参数里层层传递 userId 的麻烦。
     * 
     * @return
     */
    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 移除用户 ID
     * 理数据，防止内存泄漏和数据污染。
     * 服务器为了提高性能，通常会使用“线程池”。也就是说，当一个请求处理完毕后，这个线程并不会被销毁，而是被放回池子里，等待处理下一个人的请求。
     * 如果在请求结束时不调用 remove()，那么这个线程的“抽屉”里还会残留着上一个用户的
     * ID。当这个线程被拿去处理下一个匿名用户的请求时，系统可能会误以为还是上一个用户在操作，导致严重的越权漏洞或数据错乱。
     * 这个方法通常在拦截器的 afterCompletion 阶段（即请求处理完，准备返回给前端之前）调用
     */
    public static void removeUserId() {
        USER_THREAD_LOCAL.remove();
    }
}
