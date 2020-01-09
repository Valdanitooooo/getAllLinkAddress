var reqs = "your har file content"
var urls = ''
reqs.log.entries.forEach(item => {
    urls += '<collectionProp name="1357392467">\n   <stringProp name="726316312">' + item.request.url.replace(/&/g, '&amp;') + "</stringProp>\n</collectionProp>\n"
})
console.log(urls)