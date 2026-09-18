export function formatPrice(p) {
    return p ? '¥' + Number(p).toLocaleString() + '/月' : '价格面议'
}
