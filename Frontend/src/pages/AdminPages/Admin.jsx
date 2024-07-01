import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import "../css/user.css";
import "./admin.css";

const Admin = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const handleSignout = () => {
        localStorage.removeItem("user");
        navigate("/");
    };

    const setUserFromLocal = () => {
        const userDataString = localStorage.getItem("user");
        if (userDataString != null) {
            const user = JSON.parse(userDataString);
            setUser(user);
        }
    };

    useEffect(() => {
        setUserFromLocal();
    }, []);

    if (localStorage.getItem("user") == null || !JSON.parse(localStorage.getItem("user")).admin) {
        navigate("/");
    } else if (user && user.admin) {
        return (
            <div className="adminpage_container">
                <div className="adminpage_flex_container">
                    <Link to={"/admin/products"}>
                        Ürünler
                    </Link>
                    <Link to={"/admin/categories"}>
                        Kategoriler
                    </Link>
                    <Link to={"/admin/brands"}>
                        Markalar
                    </Link>
                    <Link to={"/admin/banners"}>
                        Bannerlar
                    </Link>
                    <Link to={"/admin/sales"}>
                        Satışlar
                    </Link>
                    <Link to={"/admin/users"}>
                        Kullanıcılar
                    </Link>
                    <Link to={"/admin/questions"}>
                        Sorular
                    </Link>
                    <Link to={"/admin/comments"}>
                        Yorumlar
                    </Link>
                </div>
                <a onClick={handleSignout}>
                    Çıkış Yap
                </a>
            </div>
        );
    }
};

export default Admin;