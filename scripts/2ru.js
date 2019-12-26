const translate = require('google-translate-api');
 
var text = '';

for (let j = 2; j < process.argv.length; j++) {  
    text += process.argv[j] + ' ';
}

//console.log("T:" + text);

if (text == '' || text == undefined) {
  console.log(":/");
} else {

translate(text, {to: 'ru'}).then(res => {
//    console.log(res.text);
    console.log('(' + res.from.language.iso + '): ' + res.text);
    //=> I speak English
    //console.log(res.from.language.iso);
    //=> nl
}).catch(err => {
    console.error(err);
});

}