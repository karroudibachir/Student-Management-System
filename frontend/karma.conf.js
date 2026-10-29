module.exports = function (config) {
  config.set({
    frameworks: ['jasmine'],
    browsers: ['ChromeHeadless'],
    reporters: ['progress'],
    singleRun: true,
    restartOnFileChange: false,
  });
};
