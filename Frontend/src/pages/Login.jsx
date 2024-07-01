import React, { useEffect, useState } from 'react'
import "./css/login.css"
import LoginForm from '../components/LoginForm'
import SignupForm from '../components/SignupForm'
import { useNavigate } from 'react-router-dom'

const Login = ({ admin, login }) => {
    const navigate = useNavigate();
    useEffect(()=>
        {
            if (localStorage.getItem("user") != null ){
                navigate("/");
            }
        }
        ,[localStorage.getItem("user")])

    if (localStorage.getItem("user") != null ){
        navigate("/");
    }
    else
        {
            return (
                <div className='user_transactions_container'>
                    {
                        admin?
                            <div className="welcome_container">
                                <div className="welcome">
                                    Admin sayfasına hoşgeldin.
                                </div>
                                <div className="info">Lütfen giriş yapmak için e postanı ve şifreni gir.</div>
                            </div>
                        :
                        <div className="welcome_container">
                            <div className="welcome">
                                {
                                    login?
                                        "Kullanıcı giriş sayfasına hoşgeldin."
                                    :
                                        "Kullanıcı kayıt sayfasına hoşgeldin."
                                }
                            </div>
                            <div className="info">
                                {
                                    login?
                                        "Lütfen giriş yapmak için e postanı ve şifreni gir."
                                    :
                                        "Lütfen kayıt oluşturmak için tüm alanları düzgün bir şekilde doldur."
                                }
                            </div>
                        </div>
                    }
                    <div className='user_transactions_form_container'>
                        {
                            login ?
                                <LoginForm tryingToGetInAs={admin ? "admin" : "user"}/>
                            :
                                <SignupForm/>
                        }
                    </div>
                </div>
            )
        }
}

export default Login