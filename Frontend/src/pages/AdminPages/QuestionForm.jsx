import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';

import "./question.css"

const QuestionForm = ({  }) => {
    const { questionId } = useParams();
    const [question, setQuestion] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        if (localStorage.getItem("user") == null || !JSON.parse(localStorage.getItem("user")).admin) {
            navigate("/");
        }
        const fetchQuestion = async () => {
        try {
            const response = await fetch(`http://localhost:8080/question?questionId=${questionId}`);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || data.message || 'An error occurred');
            } else {
                setQuestion(data);
                console.log(data);
                setLoading(false);
            }
        } catch (error) {
                console.error(error.message);
                setError(error.message);
                setLoading(false);
            }
        };
        fetchQuestion();
    }, [questionId]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        const newValue = type === 'checkbox' ? checked : value;
        setQuestion((prevQuestion) => ({
            ...prevQuestion,
            [name]: newValue
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
    
        const user = JSON.parse(localStorage.getItem("user"));
        const currentUserId = user.userId;
    
        try {
            const postBody = {
                id: question.id,
                content:question.content,
                answer:question.answer,
                isActive:question.isActive
            }
            const response = await fetch(`http://localhost:8080/update/question?currentUserId=${currentUserId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(postBody)
            });
            console.log(JSON.stringify(postBody));
    
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'An error occurred');
            }
    
            const responseData = await response.json();
            setSuccess('Soru Başarıyla Güncellendi');
            setError('');
            console.log(responseData);
        } catch (error) {
            setError(error.message);
            setSuccess('');
        }
    };

    if (loading) {
        return <p>Loading...</p>;
    }

    return (
        <div className='updateproductpage'>
            <Link to={"/admin/questions"} className='goback'>
                {"<"}Geri Dön
            </Link>
            <form onSubmit={handleSubmit} className='updateproduct'>
                <div className='bottom'>
                    <div className="left">
                        <div className='updateQuestion_content'>
                            <label htmlFor="content">Soru İçeriği </label>
                            <textarea
                                type="text"
                                id="content"
                                name="content"
                                value={question.content || ''}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className='updateQuestion_answer'>
                            <label htmlFor="answer">Cevap </label>
                            <textarea
                                type="text"
                                id="answer"
                                name="answer"
                                value={question.answer || ''}
                                onChange={handleChange}
                            />
                        </div>
                        <div className="updateQuestion_isActive">
                            <label>
                                Soru Aktif
                                <input
                                type="checkbox"
                                name="isActive"
                                checked={question.isActive}
                                onChange={handleChange}
                                />
                            </label>
                        </div>
                    </div>
                </div>
                <div className='bottom'>
                    <button type="submit">
                        Soruyu Güncelle
                    </button>
                </div>
                {error && <p className='admin_error' onClick={() => setError("")}>{error}</p>}
                {success && <p className='admin_success' onClick={() => setSuccess("")}>{success}</p>}
            </form>
        </div>
    );
};

export default QuestionForm;
