import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const SignupForm = () => {
    const [userName, setUserName] = useState('');
    const [userSurname, setUserSurname] = useState('');
    const [userMail, setUserMail] = useState('');
    const [userPassword, setUserPassword] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const requestBody = {
                user_name: userName,
                user_surname: userSurname,
                user_mail: userMail,
                user_password: userPassword,
                is_admin: false,
                is_active: true
            };

            const response = await fetch('http://localhost:8080/create/user', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) {
                const errorData = await response.json();
                console.log(errorData);
                throw new Error(errorData.error || 'An error occurred');
            }

            const userData = await response.json();
            localStorage.setItem('user', JSON.stringify(userData));
            setError('');
            window.location.href = '/user'; // Redirect to user page
        } catch (error) {
            setError(error.message);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="userName">Ad</label>
                    <input
                        type="text"
                        id="userName"
                        value={userName}
                        onChange={(e) => setUserName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="userSurname">Soyad</label>
                    <input
                        type="text"
                        id="userSurname"
                        value={userSurname}
                        onChange={(e) => setUserSurname(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="userMail">Email</label>
                    <input
                        type="email"
                        id="userMail"
                        value={userMail}
                        onChange={(e) => setUserMail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="userPassword">Şifre</label>
                    <input
                        type="password"
                        id="userPassword"
                        value={userPassword}
                        onChange={(e) => setUserPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Kayıt Ol</button>
            </form>
            <Link to={"/login"} className='login'>Giriş Yap</Link>
            <Link to={"/forgot"} className='forgot_pass'>Şifremi Unuttum</Link>
            {   
                error && 
                <p className='login_error_box' onClick={() => setError("")}>
                    {
                        error.includes("Invalid password") ? 
                        <div>
                            Lütfen düzgün bir şifre girin.
                            <div className='error_info'>
                                Şifreniz En az bir rakam, en az bir küçük harf, en az bir büyük harf, en az bir özel karakter içermeli, boşluk içermemeli, en az 6 ve en fazla 20 karakter içermeli.
                            </div>
                        </div>
                        :
                        error.includes("Invalid mail") ? 
                        <div>
                            Lütfen düzgün bir mail girin.
                        </div>
                        :
                        error.includes("mail is already in use") ?
                        <div>
                            Lütfen farklı bir mail girin.
                            <div className='error_info'>
                                Bu mail'i kullanan başka bir hesap bulunmakta.
                            </div>
                        </div>
                        :
                        error.includes("with this name and surname") ?
                        <div>
                            Böyle bir kullanıcı zaten var.
                            <div className='error_info'>
                                Bu adı ve soyadı kullanan başka bir hesap bulunmakta.
                            </div>
                        </div>
                        :
                        error
                    }
                </p>
            }
        </div>
    );
};

export default SignupForm;