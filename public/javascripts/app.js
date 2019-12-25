let myComp = new Vue({
    el: '#vue-app',

    data: {
        email: '',
        password: '',
        agreeToTerms: false,

        validEmail: true,
        validPass: true,
        validTC: true
    },

    methods: {
        postForm: function() {

            this.validEmail = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(this.email)
            this.validPass = this.password.length >= 8 && this.password.length <= 20
            this.validTC = this.agreeToTerms

            let validForm = this.validEmail && this.validPass && this.validTC

            if(validForm) {

                let postData = {
                    email: this.email,
                    password: this.password,
                    agreeToTerms: this.agreeToTerms
                }

                console.log('Sending response')

                axios.post('http://localhost:9000/v1/api/signup', postData)
                .then(function(response) {
                    console.log('Got response')
                    console.log(response)
                })
                .catch(function(error) {
                    console.log(error)
                })
            } else {
                console.log("Didn't send request as the form is valid - " + validForm)
            }
        }
    }
});
