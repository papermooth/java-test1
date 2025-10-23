#!/bin/bash

# 脚本用途：用于打包并运行微服务项目
# 功能说明：
# 1. 清理旧的日志文件
# 2. 清理并打包项目
# 3. 停止所有正在运行的服务
# 4. 启动所有服务
# 5. 显示服务启动信息和访问命令

echo "===================================="
echo "Maven打包并运行jar脚本"
echo "===================================="
echo "注意：请确保已安装JDK 1.8+、Maven和ZooKeeper"
echo ""

# 清理旧日志文件的函数
clean_log_files() {
    echo "清理旧的日志文件..."
    # 清理各个服务的日志文件
    rm -f /root/test1/demo-provider/target/provider_jar.log
    rm -f /root/test1/order-service/target/order_service_jar.log
    rm -f /root/test1/payment-service/target/payment_service_jar.log
    rm -f /root/test1/analytics-service/target/analytics_service_jar.log
    rm -f /root/test1/demo-webapp/target/webapp_jar.log
    echo "✓ 日志文件清理完成"
}

# 检查ZooKeeper是否运行
check_zk() {
    echo "检查ZooKeeper是否运行..."
    if ! nc -z localhost 2181; then
        echo "错误：ZooKeeper未在端口2181上运行"
        echo "请先启动ZooKeeper服务"
	echo " /root/test1/zoo/apache-zookeeper-3.9.4-bin/bin/zkServer.sh start" 
        exit 1
    else
        echo "✓ ZooKeeper正在运行"
    fi
}

# 清理并打包项目
package_project() {
    echo ""
    echo "开始清理并打包项目..."
    cd /root/test1
    
    echo "打包Demo API模块..."
    cd /root/test1/demo-api
    mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：Demo API模块打包失败"
        exit 1
    fi
    
    echo "打包服务提供者..."
    cd /root/test1/demo-provider
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：服务提供者打包失败"
        exit 1
    fi
    
    echo "打包订单API模块..."
    cd /root/test1/order-api
    mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：订单API模块打包失败"
        exit 1
    fi
    
    echo "打包支付API模块..."
    cd /root/test1/payment-api
    mvn clean install -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：支付API模块打包失败"
        exit 1
    fi
    
    echo "打包订单服务..."
    cd /root/test1/order-service
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：订单服务打包失败"
        exit 1
    fi
    
    echo "打包支付服务..."
    cd /root/test1/payment-service
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：支付服务打包失败"
        exit 1
    fi
    
    echo "打包数据分析服务..."
    cd /root/test1/analytics-service
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：数据分析服务打包失败"
        exit 1
    fi
    
    echo "打包Web应用..."
    cd /root/test1/demo-webapp
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误：Web应用打包失败"
        exit 1
    fi
    
    echo "✓ 所有项目打包成功"
}

# 运行服务提供者jar
run_provider() {
    echo ""
    echo "停止之前的服务进程..."
    # 停止之前的服务进程
    pkill -f "demo-provider"
    sleep 2
    
    echo "运行服务提供者jar（使用SkyWalking代理）..."
    cd /root/test1/demo-provider/target
    nohup java --add-opens java.base/java.lang=ALL-UNNAMED -jar demo-provider-1.0-SNAPSHOT.jar > provider_jar.log 2>&1 &
    PROVIDER_PID=$!
    echo "服务提供者已启动，PID: $PROVIDER_PID"
    echo "日志文件: provider_jar.log"
}

# 运行订单服务jar
run_order_service() {
    echo ""
    echo "停止之前的订单服务进程..."
    # 停止之前的订单服务进程
    pkill -f "order-service"
    sleep 2
    
    echo "运行订单服务jar..."
    cd /root/test1/order-service/target
    nohup java --add-opens java.base/java.lang=ALL-UNNAMED -jar order-service-1.0-SNAPSHOT.jar > order_service_jar.log 2>&1 &
    ORDER_SERVICE_PID=$!
    echo "订单服务已启动，PID: $ORDER_SERVICE_PID"
    echo "日志文件: order_service_jar.log"
}

# 运行支付服务jar
run_payment_service() {
    echo ""
    echo "停止之前的支付服务进程..."
    # 停止之前的支付服务进程
    pkill -f "payment-service"
    sleep 2
    
    echo "运行支付服务jar..."
    cd /root/test1/payment-service/target
    nohup java --add-opens java.base/java.lang=ALL-UNNAMED -jar payment-service-1.0-SNAPSHOT.jar > payment_service_jar.log 2>&1 &
    PAYMENT_SERVICE_PID=$!
    echo "支付服务已启动，PID: $PAYMENT_SERVICE_PID"
    echo "日志文件: payment_service_jar.log"
}

# 运行数据分析服务jar
run_analytics_service() {
    echo ""
    echo "停止之前的数据分析服务进程..."
    # 停止之前的数据分析服务进程
    pkill -f "analytics-service"
    sleep 2
    
    echo "运行数据分析服务jar..."
    cd /root/test1/analytics-service/target
    nohup java --add-opens java.base/java.lang=ALL-UNNAMED -jar analytics-service-1.0-SNAPSHOT.jar > analytics_service_jar.log 2>&1 &
    ANALYTICS_SERVICE_PID=$!
    echo "数据分析服务已启动，PID: $ANALYTICS_SERVICE_PID"
    echo "日志文件: analytics_service_jar.log"
}

# 运行Web应用jar
run_webapp() {
    echo ""
    echo "停止之前的Web应用进程..."
    # 停止之前的Web应用进程
    pkill -f "demo-webapp"
    sleep 2
    
    echo "运行Web应用jar..."
    # 等待其他服务启动完成
    sleep 5
    cd /root/test1/demo-webapp/target
    nohup java --add-opens java.base/java.lang=ALL-UNNAMED -jar demo-webapp-1.0-SNAPSHOT.jar > webapp_jar.log 2>&1 &
    WEBAPP_PID=$!
    echo "Web应用已启动，PID: $WEBAPP_PID"
    echo "日志文件: webapp_jar.log"
}

# 停止所有服务的函数
stop_all_services() {
    echo "停止所有服务..."
    # 停止Web应用
    if [ ! -z "$WEBAPP_PID" ]; then
        echo "停止Web应用（PID: $WEBAPP_PID）..."
        kill $WEBAPP_PID 2>/dev/null || echo "Web应用可能已停止"
        wait $WEBAPP_PID 2>/dev/null
    else
        # 尝试通过进程名查找并停止Web应用
        WEBAPP_PID=$(ps aux | grep "demo-webapp-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
        if [ ! -z "$WEBAPP_PID" ]; then
            echo "停止Web应用（PID: $WEBAPP_PID）..."
            kill $WEBAPP_PID 2>/dev/null
        fi
    fi
    
    # 停止数据分析服务
    if [ ! -z "$ANALYTICS_SERVICE_PID" ]; then
        echo "停止数据分析服务（PID: $ANALYTICS_SERVICE_PID）..."
        kill $ANALYTICS_SERVICE_PID 2>/dev/null || echo "数据分析服务可能已停止"
        wait $ANALYTICS_SERVICE_PID 2>/dev/null
    else
        # 尝试通过进程名查找并停止数据分析服务
        ANALYTICS_PID=$(ps aux | grep "analytics-service-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
        if [ ! -z "$ANALYTICS_PID" ]; then
            echo "停止数据分析服务（PID: $ANALYTICS_PID）..."
            kill $ANALYTICS_PID 2>/dev/null
        fi
    fi
    
    # 停止支付服务
    if [ ! -z "$PAYMENT_SERVICE_PID" ]; then
        echo "停止支付服务（PID: $PAYMENT_SERVICE_PID）..."
        kill $PAYMENT_SERVICE_PID 2>/dev/null || echo "支付服务可能已停止"
        wait $PAYMENT_SERVICE_PID 2>/dev/null
    else
        # 尝试通过进程名查找并停止支付服务
        PAYMENT_PID=$(ps aux | grep "payment-service-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
        if [ ! -z "$PAYMENT_PID" ]; then
            echo "停止支付服务（PID: $PAYMENT_PID）..."
            kill $PAYMENT_PID 2>/dev/null
        fi
    fi
    
    # 停止订单服务
    if [ ! -z "$ORDER_SERVICE_PID" ]; then
        echo "停止订单服务（PID: $ORDER_SERVICE_PID）..."
        kill $ORDER_SERVICE_PID 2>/dev/null || echo "订单服务可能已停止"
        wait $ORDER_SERVICE_PID 2>/dev/null
    else
        # 尝试通过进程名查找并停止订单服务
        ORDER_PID=$(ps aux | grep "order-service-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
        if [ ! -z "$ORDER_PID" ]; then
            echo "停止订单服务（PID: $ORDER_PID）..."
            kill $ORDER_PID 2>/dev/null
        fi
    fi
    
    # 停止服务提供者
    if [ ! -z "$PROVIDER_PID" ]; then
        echo "停止服务提供者（PID: $PROVIDER_PID）..."
        kill $PROVIDER_PID 2>/dev/null || echo "服务提供者可能已停止"
        wait $PROVIDER_PID 2>/dev/null
    else
        # 尝试通过进程名查找并停止服务提供者
        PROVIDER_PID=$(ps aux | grep "demo-provider-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
        if [ ! -z "$PROVIDER_PID" ]; then
            echo "停止服务提供者（PID: $PROVIDER_PID）..."
            kill $PROVIDER_PID 2>/dev/null
        fi
    fi
    
    # 清理PID变量
    unset WEBAPP_PID ANALYTICS_SERVICE_PID PAYMENT_SERVICE_PID ORDER_SERVICE_PID PROVIDER_PID
    
    echo "✓ 所有服务已停止或不存在"
}

# 主函数
main() {
    check_zk
    clean_log_files
    package_project
    run_provider
    run_order_service
    run_payment_service
    run_analytics_service
    run_webapp
    
    echo ""
    echo "===================================="
    echo "所有服务已通过jar包方式启动！"
    echo "===================================="
    echo "服务访问地址: http://localhost:8081/hello/USER"
    echo "使用以下命令测试复杂调用链路:"
    echo "curl http://localhost:8081/complex-flow/testuser"
    echo ""
    echo "如需停止服务，请使用以下方法："
    echo "1. 调用脚本的stop_all_services函数: $0 stop"
    echo "2. 或者使用kill命令: kill -9 $PROVIDER_PID $ORDER_SERVICE_PID $PAYMENT_SERVICE_PID $ANALYTICS_SERVICE_PID $WEBAPP_PID"
}

# 处理命令行参数
if [ "$1" = "stop" ]; then
    stop_all_services
    exit 0
fi

# 执行主函数
main
