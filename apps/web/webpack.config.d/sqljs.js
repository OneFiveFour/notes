// SQL.js loads its Wasm binary relative to the emitted worker.
const CopyWebpackPlugin = require('copy-webpack-plugin');
config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign({}, config.resolve.fallback, {
    fs: false,
    path: false,
    crypto: false,
});
config.plugins.push(new CopyWebpackPlugin({
    patterns: [{ from: require.resolve('sql.js/dist/sql-wasm.wasm'), to: 'sql-wasm.wasm' }],
}));
