document.addEventListener("DOMContentLoaded", () => {
    const editor = new EditorJS({
        holderId: 'editorjs',

        tool: {
            header: {
                class: Header,
                inlineToolbar: ['link']
            },
            list: {
                class: List,
                inlineToolbar: [
                    'link',
                    'bold'
                ]
            },
            embed: {
                class: Embed,
                inlineToolbar: false,
                config: {
                    services: {
                        youtube: true
                    }
                }
            },
        },
    })
})

let saveBtn = document.getElementById("save-btn");
saveBtn.addEventListener("click", function (){
    editor.saver().then((outputData) => {
        console.log('Article data: ', outputData)
    }).catch(error => {
        console.log('saving failed: ',error)
    })
})