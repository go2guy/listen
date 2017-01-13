exports.config = {
    LOG_FILE: "/interact/logs/cdrpostsimulator.log",
    LOG_LEVEL: "debug",
    HTTP_PORT: 8100,
    REQUESTS: [
        { code: 200, percent: 80 },
        { code: 500, percent: 20 }
    ]
};