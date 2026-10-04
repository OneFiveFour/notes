// Serve SQL.js Wasm next to the worker during both JS and WasmJS browser tests.
const binary = require.resolve('sql.js/dist/sql-wasm.wasm').replace(/\\/g, '/');
config.files.push({ pattern: binary, served: true, watched: false, included: false, nocache: false });
config.proxies['/sql-wasm.wasm'] = '/absolute' + binary;
config.webpack.resolve = config.webpack.resolve || {};
config.webpack.resolve.fallback = Object.assign({}, config.webpack.resolve.fallback, {
    fs: false,
    path: false,
    crypto: false,
});
