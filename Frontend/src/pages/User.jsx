import React, { useEffect, useState } from 'react';
import "./css/user.css";
import { useNavigate } from 'react-router-dom';

const User = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [formattedDate, setFormattedDate] = useState("");

    const handleSignout = () => {
        localStorage.removeItem("user");
        navigate("/");
    };

    const setUserFromLocal = () => {
        const userDataString = localStorage.getItem("user");
        if (userDataString != null) {
            const user = JSON.parse(userDataString);
            setUser(user);
            const date = new Date(user.createdAt).toLocaleString();
            setFormattedDate(date);
        }
    };

    useEffect(() => {
        setUserFromLocal();
    }, []);

    return (
        <div className='user_data_container'>
            <h2>
                {user ? "Hesap Detayları" : "Lütfen önce giriş yapın!"}
            </h2>
            {user && (
                <div className='user_data_flex_container'>
                    <div className='user_data_props'>
                        <ul className='left'>
                            {user.admin && <li><strong>User ID </strong></li>}
                            <li><strong>Ad </strong></li>
                            <li><strong>Soyad </strong></li>
                            <li><strong>Eposta </strong></li>
                            {user.admin && <li><strong>Şifre </strong></li>}
                            <li><strong>Bakiye </strong></li>
                            <li><strong>Hesap Oluşturulma Tarihi </strong></li>
                            {user.admin && (
                                <div>
                                    <li><strong>Admin </strong></li>
                                </div>
                            )}
                        </ul>
                    </div>
                    <div className='user_data_contents'>
                        <ul className='right'>
                            {user.admin && <li>{user.userId}</li>}
                            <li>{user.userName}</li>
                            <li>{user.userSurname}</li>
                            <li>{user.userMail}</li>
                            {user.admin && <li>{user.userPassword}</li>}
                            <li>{user.userBalance} TL</li>
                            <li>{formattedDate}</li>
                            {user.admin && (
                                <div>
                                    <li>{user.admin ? 'Evet' : 'Hayır'}</li>
                                </div>
                            )}
                        </ul>
                    </div>
                </div>
            )}
            {user && (
                <div style={{display:"flex"}}>
                    <button onClick={()=> navigate("/purchases")} className='viewpurchases'>
                        Siparişleri Görüntüle
                    </button>
                    <button onClick={handleSignout}>
                        Çıkış Yap
                    </button>
                </div>
            )}
        </div>
    );
};

export default User;