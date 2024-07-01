import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const AdminQuestions = () => {
    const [questions, setQuestions] = useState(null);
    const [loading, setLoading] = useState(false);
    const [page,setPage] = useState(0);

    const navigate = useNavigate();

    useEffect(() => {
        if (localStorage.getItem("user") == null || !JSON.parse(localStorage.getItem("user")).admin) {
            navigate("/login");
        }
        const fetchQuestions = async () => {
            setLoading(true);
            try {
                const response = await fetch(`http://localhost:8080/questions?page=${page}&isHereToAnswer=true`);
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const data = await response.json();
                setQuestions(data);
            } 
            catch (error) {
                console.error('Error fetching search results:', error);
            } 
            finally {
                setLoading(false);
            }
        };

        fetchQuestions();
    }, [page, localStorage.getItem("user")]);
    
    return (
        localStorage.getItem("user") != null && JSON.parse(localStorage.getItem("user")).admin && questions && !loading ?
        <div className='adminpages_container'>
            <div className="adminpages_flex_container upper">
                <div className="admin_id">
                    ID
                </div>
                <div className="admin_username">
                    Kullanıcı Adı
                </div>
                <div className="admin_mail">
                    Kullanıcı Maili
                </div>
                <div className="admin_name">
                    Ürün Adı
                </div>
                <div className="admin_mail">
                    Admin Maili
                </div>
                <div className="admin_question">
                    Soru
                </div>
                <div className="admin_answer">
                    Cevap
                </div>
                <div className="admin_isactive">
                    Aktiflik Durumu
                </div>
                <div className="admin_editanswer">
                    Cevapla/Düzenle
                </div>
            </div>
            {
                questions.content.map(
                    (item) => {
                        return (
                            <div className="adminpages_flex_container">
                                <div className="admin_id">
                                    {item.id}
                                </div>
                                <div className="admin_username">
                                    {item.user.userName}
                                </div>
                                <div className="admin_mail">
                                    {item.user.userMail}
                                </div>
                                <div className="admin_name">
                                    {item.product.productName}
                                </div>
                                <div className="admin_mail">
                                {
                                        item.admin ?
                                            item.admin.userMail
                                        :
                                            "Yok"
                                    }
                                </div>
                                <div className="admin_question">
                                    {item.content}
                                </div>
                                <div className="admin_answer">
                                    {
                                        item.answer ? item.answer : "Yok"
                                    }
                                </div>
                                <div className="admin_isactive">
                                    {
                                        item.active ? "Evet":"Hayır"
                                    }
                                </div>
                                <div className="admin_editanswer">
                                    <Link to={`/admin/questions/edit/${item.id}`}>
                                        Cevapla/Düzenle
                                    </Link>
                                </div>
                            </div>
                        )
                    }
                )
            }
            <div className='adminpages_page_controller'>
                <button className={questions.first? "":"active"} 
                    onClick={() =>
                        {
                            if (!questions.first) {
                                setPage(page-1);
                            }
                        }
                    }
                >
                    {"<"}
                </button>
                <div className={`adminpages_current_page${!questions.last || !questions.first ? " active":""}`}>
                    {page}
                </div>
                <button className={questions.last? "":"active"} 
                    onClick={() =>
                        {
                            if (!questions.last) {
                                setPage(page+1);
                            }
                        }
                    }
                >
                    {">"}
                </button>
            </div>
        </div>
        :
        localStorage.getItem("user") != null && JSON.parse(localStorage.getItem("user")).admin  ?
        <div className="">
            Loading
        </div>
        :
        navigate("/login")
    )
}

export default AdminQuestions