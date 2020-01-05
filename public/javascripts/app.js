let myComp = new Vue({
    el: '#vue-app',

    data: {
        menuDisplayed: false,
        showSignupForm: true,
        showSignupSuccess: false,
        showLoginForm: false,

        email: '',
        password: '',
        agreeToTerms: false,

        logInEmail: '',
        logInPassword: '',

        validEmail: true,
        validPass: true,
        validTC: true,

        invalidFormMessage: ''
    },

    methods: {

        displaySignUpForm: function() {
            this.showSignupSuccess = false;
            this.showSignupForm = true;
            this.showLoginForm = false;
            this.menuDisplayed = false;
            this.invalidFormMessage = '';
        },

        displayMenu: function() {
            console.log('Showing menu')
            this.menuDisplayed = !this.menuDisplayed;
        },

        displayLoginForm: function() {
            this.showSignupSuccess = false;
            this.showSignupForm = false;
            this.showLoginForm = true;
            this.menuDisplayed = false;
            this.invalidFormMessage = '';
        },

        postSignupForm: function() {

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

                axios.post('http://localhost:9000/v1/api/signup', postData)
                .then(response => {
                    this.showSignupForm = false;
                    this.showSignupSuccess = true;
                })
                .catch(error => {
                    this.invalidFormMessage = error.response.data.error;
                })
            }
        },

        postLoginForm: function() {
            let postData = {
                email: this.logInEmail,
                password: this.logInPassword
            }

            axios.post('http://localhost:9000/v1/api/login', postData)
            .then(response => {
                this
            })
            .catch(error => {
                this.invalidFormMessage = error.response.data.error;
            })
        }
    }
});
