import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const LoginForm = ( { tryingToGetInAs } ) => {
    const [user, setUser] = useState();
    const [userMail, setUserMail] = useState('');
    const [userPassword, setUserPassword] = useState('');
    const [error, setError] = useState('');

    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const response = await fetch(`http://localhost:8080/${tryingToGetInAs}?userMail=${userMail}&userPassword=${userPassword}`, {
                method: 'POST',
                headers: {
                'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'An error occurred');
            }
            const userData = await response.json();
            localStorage.setItem('user', JSON.stringify(userData));
            setUser(response.data);
            setError('');
            if (userData.admin) navigate("/admin");
            else navigate("/user");
        } catch (error) {
            setError(error.message);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="userMail">Email </label>
                    <input
                        type="email"
                        id="userMail"
                        value={userMail}
                        onChange={(e) => setUserMail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="userPassword">Şifre </label>
                    <input
                        type="password"
                        id="userPassword"
                        value={userPassword}
                        onChange={(e) => setUserPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Giriş Yap</button>
            </form>
            <Link to={"/signup"}>Kayıt Ol</Link>
            <Link to={"/forgot"} className='forgot_pass'>Şifremi Unuttum</Link>
            {   
                error && 
                <p className='login_error_box' onClick={() => setError("")}>
                    {error =="There is no account with this mail." ? "Lütfen Mailinizi doğru girdiğinizden emin olun" 
                    : error == "Injection detected." ? "Injection tespit edildi! IP adresiniz kaydedildi..." 
                    : error == "Wrong password." ? "Şifreniz yanlış..."
                    : error.includes("admins login") ? "Bu giriş'e erişiminiz yok..."
                    : error.includes("users login") ? "Lütfen admin/login'den giriş yapın"
                    : error
                    }
                </p>
            }
            {user && <p>Welcome, {user.userName}!</p>}
        </div>
    );
};

export default LoginForm;
